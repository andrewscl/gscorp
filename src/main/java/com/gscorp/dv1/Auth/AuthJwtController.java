package com.gscorp.dv1.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.services.SignInService;
import com.gscorp.dv1.services.SignUpService;
import com.gscorp.dv1.users.infrastructure.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthJwtController {
    
    private final SignUpService signUpService;
    private final SignInService signInService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        signUpService.register(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = signInService.signin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            //logging interno
            System.out.println("Error de autenticación: " +
                e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity
                .status(401)
                .body("Error interno: " + e.getClass().getSimpleName()
                            + e.getMessage());
        }
    }
}
