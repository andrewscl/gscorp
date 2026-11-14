package com.gscorp.dv1.auth.web;

import java.util.Optional;
import com.gscorp.dv1.auth.application.PasswordRecoveryService;
import com.gscorp.dv1.auth.web.dto.PasswordRecoveryRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.auth.application.PasswordResetTokenService;
import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.SetPasswordRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {

        private final PasswordResetTokenService passwordResetTokenService;
        private final UserService userService;
        private final PasswordEncoder passwordEncoder;
        private final PasswordRecoveryService passwordRecoveryService;

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
                user.setStatus(UserStatus.ACTIVE);
                userService.save(user);
                // 3. Marcar el token como usado
                passwordResetTokenService.markAsUsed(prt);
                return ResponseEntity.ok("Contraseña definida correctamente");
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<String> forgotPassword(@RequestBody PasswordRecoveryRequest request) {
                try {
                        var recovery = passwordRecoveryService.prepareRecovery(request.usernameOrEmail());
                        recovery.ifPresent(passwordRecoveryService::executeRecovery);
                } catch (IllegalStateException e) {
                        log.error("Error during password recovery", e);
                }

                return ResponseEntity.ok("Si existe un usuario con ese correo o nombre de usuario, " +
                                        "se enviará un enlace de recuperación.");
        }
}
