package com.gscorp.dv1.users.web;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.auth.application.PasswordResetTokenService;
import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;
import com.gscorp.dv1.users.web.dto.SetPasswordRequest;
import com.gscorp.dv1.users.web.dto.UserDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserRestController {

        private final UserService userService;
        private final PasswordResetTokenService passwordResetTokenService;
        private final PasswordEncoder passwordEncoder;

        @PostMapping("/create")
        public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
            Long id = userService.createUser(req);
            return ResponseEntity
                    .created(URI.create("/api/users/" + id)).build();
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable Long id) {
                userService.deleteById(id);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/{id}")
        public ResponseEntity<UserDto> getById(@PathVariable Long id) {
                User user = userService.findById(id);
                return ResponseEntity.ok(UserDto.fromEntity(user));
        }

        @PostMapping("/set-password")
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
