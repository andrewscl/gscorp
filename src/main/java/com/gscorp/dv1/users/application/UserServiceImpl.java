package com.gscorp.dv1.users.application;

import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.roles.infrastructure.RoleRepository;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.infrastructure.UserRepository;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    //Crear usuario
    public Long createUser (CreateUserRequest req){
        if(req.username()==null || req.username().isBlank())
            throw new IllegalArgumentException("username requerido");
        if(req.password()==null || req.password().isBlank())
            throw new IllegalArgumentException("password requerido");
        if(userRepo.findByUsername(req.username()).isPresent())
            throw new DataIntegrityViolationException("El usuario ya existe");

        var u = new User();
        u.setUsername(req.username());
        u.setMail(req.mail());
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

    //Eliminar usuario
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepo.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        try {
            userRepo.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("No se puede eliminar: el usuario tiene referencias");
        }
    }

    @Override
    public List<User> findAll(){
        return userRepo.findAll();
    }

    @Override
    public User findById(Long id){
        return userRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Override
    public User findWithClientsById(Long id){
        return userRepo.findWithClientsById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado" + id));
    }

    @Override
    public List<User> findAllWithRolesAndClients(){
        return userRepo.findAllWithRolesAndClients();
    }
}
