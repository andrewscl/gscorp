package com.gscorp.dv1.users.application;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientRepository;
import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.roles.infrastructure.RoleRepository;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.infrastructure.UserRepository;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final ClientRepository clientRepo;
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
        u.setClients(new HashSet<>());

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

        if (req.clientIds()!=null && !req.clientIds().isEmpty()) {
            List<Client> clients = clientRepo.findAllById(req.clientIds());
            if(clients.isEmpty())
                throw new IllegalArgumentException("Clientes invalidos");
            u.getClients().addAll(clients);
        } else {
            /*Cliente por defecto
            Client def = clientRepo.findByName("CLIENT")
                .orElseThrow(() ->new IllegalStateException("Rol Client no existe"));
            u.getRoles().add(def);*/
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
    public User findWithRolesAndClientsById(Long id){
        return userRepo.findWithRolesAndClientsById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado" + id));
    }

    @Override
    public List<User> findAllWithRolesAndClients(){
        return userRepo.findAllWithRolesAndClients();
    }

    @Override
    public User createInvitedUser(InviteUserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setMail(request.mail());
        user.setActive(true);

        // Asignar roles
        Set<Role> roles = new HashSet<>();
        if (request.roleIds() != null) {
            for (Long roleId : request.roleIds()) {
                    roleRepo.findById(roleId).ifPresent(roles::add);
            }
        }
        user.setRoles(roles);

        // Asignar clientes
        Set<Client> clients = new HashSet<>();
        if (request.clientIds() != null) {
            for (Long clientId : request.clientIds()) {
                clientRepo.findById(clientId).ifPresent(clients::add);
            }
        }
        user.setClients(clients);

        //NO contraseña aun, ni token (El controller gestiona eso)
        user.setPassword(null);
        user.setInvitationToken(null);
        user.setInvitationTokenExpiry(null);

        return userRepo.save(user);
    }

    //Valida token de invitación (para frontend)
    @Override
    public Boolean isInvitationTokenValid(String token) {
        Optional<User> userOpt = userRepo.findByInvitationToken(token);
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();
        return user.getInvitationTokenExpiry() != null && 
               user.getInvitationTokenExpiry().isAfter(LocalDateTime.now());
    }

    //Define la contraseña a partir del token de invitación
    @Override
    public Boolean setPasswordFromInvitation(String token, String password){
        Optional<User> userOpt = userRepo.findByInvitationToken(token);
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();
        if (user.getInvitationTokenExpiry() == null || 
            user.getInvitationTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }
        user.setPassword(encoder.encode(password));
        //Invalida el token
        user.setInvitationToken(null);
        user.setInvitationTokenExpiry(null);
        user.setActive(true);
        userRepo.save(user);
        return true;
    }

    @Override
    public void save(User user) {
        userRepo.save(user);
    }
}
