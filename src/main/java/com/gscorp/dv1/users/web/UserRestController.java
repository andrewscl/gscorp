package com.gscorp.dv1.users.web;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.entities.User;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;
import com.gscorp.dv1.users.web.dto.UserDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserRestController {

        private final UserService userService;

        @PostMapping("/create")
        public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
            Long id = userService.createUser(req);
            return ResponseEntity
                    .created(URI.create("/api/users" + id)).build();
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
    
}
