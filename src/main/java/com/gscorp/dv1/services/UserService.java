package com.gscorp.dv1.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gscorp.dv1.entities.User;
import com.gscorp.dv1.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUser (User user) {

        //Validar que no existe el usuario
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            throw new RuntimeException(
                "Username already exists");
        };

        //Condificar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //Guardar el usuario
        userRepository.save(user);
    }
}
