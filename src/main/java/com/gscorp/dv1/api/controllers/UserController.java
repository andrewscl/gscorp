package com.gscorp.dv1.api.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.api.dto.CreateUserRequest;
import com.gscorp.dv1.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserController {

        private final UserService userService;

        @PostMapping("/create")
        public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
            Long id = userService.createUser(req);
            return ResponseEntity
                    .created(URI.create("/private/admin/users" + id)).build();
        }
    
}
