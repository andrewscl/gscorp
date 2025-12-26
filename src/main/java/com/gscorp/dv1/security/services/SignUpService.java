package com.gscorp.dv1.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.infrastructure.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public void register (User user) {
        
        //Validar que no exista el username
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
                            throw new RuntimeException(
                                "Username alreaday exists.");
                            };

        //codificar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //Guardar el Usuario
        userRepository.save(user);
    }
}
