package in.bushansigur.moneymanager.controller;

import in.bushansigur.moneymanager.dto.AuthDTO;
import in.bushansigur.moneymanager.dto.ProfileDTO;
import in.bushansigur.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDTO> registerProfile(@RequestBody ProfileDTO profileDTO){
        ProfileDTO registerProfile = profileService.registerProfile(profileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerProfile);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam("token") String activationToken) {

        if (activationToken == null) {
            return ResponseEntity.badRequest().body("Missing activation token");
        }
        boolean activated = profileService.activateProfile(activationToken);

        if (activated) {
            return ResponseEntity.status(HttpStatus.OK).body("Profile activated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired activation token.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody AuthDTO authDTO) {
       try{
           if(!profileService.isAccountActive(authDTO.getEmail())){
               return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                       "message","Account is not active. Please account active first"
               ));
           }
           Map<String,Object> response = profileService.authenticateAndGenerateToken(authDTO);
           return ResponseEntity.status(HttpStatus.OK).body(response);

       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                   "message",e.getMessage()
           ));
       }
    }

    @GetMapping("/test")
    public String test(){
        return "testing";
    }
}
