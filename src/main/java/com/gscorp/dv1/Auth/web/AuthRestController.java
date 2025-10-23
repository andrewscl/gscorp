package com.gscorp.dv1.auth.web;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.auth.application.PasswordResetTokenService;
import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.SetPasswordRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {

        private final PasswordResetTokenService passwordResetTokenService;
        private final UserService userService;
        private final PasswordEncoder passwordEncoder;

        @PostMapping("/set-password")
        @Transactional
        public ResponseEntity<?> setPassword(@RequestBody SetPasswordRequest request) {

                    // 1. Buscar el token
                Optional<PasswordResetToken> opt = passwordResetTokenService.findByToken(request.token());

                if (opt.isEmpty() || !passwordResetTokenService.isValid(opt.get())) {
                        return ResponseEntity.badRequest().body("El enlace es inválido o ha expirado.");
                }

                PasswordResetToken prt = opt.get();
                User user = prt.getUser();

                // 2. Cambiar la contraseña (encriptada)
                user.setPassword(passwordEncoder.encode(request.password()));
                userService.save(user);

                // 3. Marcar el token como usado
                passwordResetTokenService.markAsUsed(prt);

                return ResponseEntity.ok("Contraseña definida correctamente");

        }
}
