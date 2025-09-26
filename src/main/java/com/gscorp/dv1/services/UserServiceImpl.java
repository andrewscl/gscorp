package com.gscorp.dv1.services;

import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gscorp.dv1.api.dto.CreateUserRequest;
import com.gscorp.dv1.entities.Role;
import com.gscorp.dv1.entities.User;
import com.gscorp.dv1.repositories.RoleRepository;
import com.gscorp.dv1.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public Long createUser (CreateUserRequest req){
        if(req.username()==null || req.username().isBlank())
            throw new IllegalArgumentException("username requerido");
        if(req.password()==null || req.password().isBlank())
            throw new IllegalArgumentException("password requerido");
        if(userRepo.findByUsername(req.username()).isPresent())
            throw new DataIntegrityViolationException("El usuario ya existe");

        var u = new User();
        u.setUsername(req.username());
        u.setPassword(encoder.encode(req.password()));
        u.setRoles(new HashSet<>());

        if (req.roleIds()!=null && !req.roleIds().isEmpty()) {
            List<Role> roles = roleRepo.findAllById(req.roleIds());
            if(roles.isEmpty())
                throw new IllegalArgumentException("Roles invalidos");
            u.getRoles().addAll(roles);
        } else {
            //Rol por defecto
            Role def = roleRepo.findByRole("CLIENT")
                .orElseThrow(() ->new IllegalStateException("Rol Client no existe"));
            u.getRoles().add(def);
        }

        return userRepo.save(u).getId();
        
    }
    
}
