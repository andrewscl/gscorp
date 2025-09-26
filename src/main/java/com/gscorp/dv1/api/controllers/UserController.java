package com.gscorp.dv1.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.entities.User;
import com.gscorp.dv1.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

        private final UserService userService;

        @PostMapping("/create")
        public ResponseEntity <?> createUser(@RequestBody User user) {
            userService.createUser(user);
            return ResponseEntity.ok("Usuario creado correctamente.");
        }
    
}
