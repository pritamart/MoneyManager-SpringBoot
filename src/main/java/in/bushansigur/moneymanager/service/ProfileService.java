package in.bushansigur.moneymanager.service;

import ch.qos.logback.core.CoreConstants;
import in.bushansigur.moneymanager.dto.AuthDTO;
import in.bushansigur.moneymanager.dto.ProfileDTO;
import in.bushansigur.moneymanager.entity.ProfileEntity;
import in.bushansigur.moneymanager.repository.ProfileRepository;
import in.bushansigur.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);
        // send activation email
        String activationLink = "http://localhost:9090/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Active Profile Money Manager account";
        String body = "Active Profile Money Manager account  " + activationLink;
        emailService.sendEmail(newProfile.getEmail(), subject, body);


        return toDTO(newProfile);
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return  ProfileEntity.builder()
                .id(profileDTO.getId())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .fullName(profileDTO.getFullName())
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }
    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .email(profileEntity.getEmail())
                .password(profileEntity.getPassword())
                .fullName(profileEntity.getFullName())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profile.setActivationToken(null); // optional: clear token
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }
    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(()-> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    public ProfileDTO getCurrentProfileDTO(String email) {
        ProfileEntity currentUser = null;
        if(email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(()-> new UsernameNotFoundException("Profile not found with email: " + email));
        }
        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();

    }

    public Map<String,Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword())
            );

            String jwtToken = jwtUtil.generateToken(authDTO.getEmail());
            ProfileEntity user = profileRepository.findByEmail(authDTO.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authDTO.getEmail()));

            return Map.of("token", jwtToken, "user", toDTO(user));

        } catch (DisabledException e) {
            throw new UsernameNotFoundException("Account is not activated");
        } catch (BadCredentialsException e) {
            throw new UsernameNotFoundException("Invalid email or password");
        } catch (Exception e) {
//            e.printStackTrace();
            throw new UsernameNotFoundException("Authentication failed: " + e.getMessage());
        }
    }

}
