package in.bushansigur.moneymanager.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret; // from application.properties

    @Value("${jwt.expiration-ms:3600000}")
    private long jwtExpirationMs; // default: 1 hour

    private Key signingKey;

    /**
     * Initializes signing key after the bean is created.
     * Handles base64 and raw string secrets safely.
     */
    @PostConstruct
    private void init() {
        System.out.println("Initializing JWT Util...");
        System.out.println("JWT Expiration (ms): " + jwtExpirationMs);

        byte[] keyBytes;
        try {
            // Try base64 decode
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            // fallback to raw bytes
            keyBytes = secret.getBytes();
        }

        // Ensure key is at least 256 bits (32 bytes)
        if (keyBytes.length < 32) {
            System.out.println("⚠️ JWT secret too short (" + keyBytes.length * 8 + " bits). Padding to 256 bits...");
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            for (int i = keyBytes.length; i < 32; i++) {
                padded[i] = (byte) (i * 7); // arbitrary filler to reach 256 bits
            }
            keyBytes = padded;
        }

        signingKey = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("✅ JWT secret key initialized successfully.");
    }

    // ---------------- TOKEN GENERATION ---------------- //

    public String generateToken(String subject) {
        return generateToken(Map.of(), subject);
    }

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ---------------- CLAIM EXTRACTION ---------------- //

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ---------------- VALIDATION ---------------- //

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ---------------- CLAIM PARSING ---------------- //

    private Claims parseAllClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }

    public Object getClaimByName(String token, String claimName) {
        return parseAllClaims(token).get(claimName);
    }
}
