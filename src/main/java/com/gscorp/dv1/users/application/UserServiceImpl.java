package com.gscorp.dv1.users.application;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.auth.application.PasswordResetTokenService;
import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientRepository;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.infrastructure.EmployeeRepository;
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
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmployeeRepository employeeRepo;

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

        //Eliminar los tokens asociados al usuario
        passwordResetTokenService.deleteByUserId(id);

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

        //Asociar empleado
        if(request.employeeId() != null) {
            Employee employee = employeeRepo.findById(request.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                                        "Empleado no encontrado"));
            if(employee.getUser() != null) {
                throw new IllegalArgumentException(
                    "Empleado ya asociado a un usuario");
            }
            employeeRepo.findById(request.employeeId())
                .ifPresent(user::setEmployee);
        }

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

    @Override
    public Boolean isAdmin(User user) {
        return user.getRoles().stream()
            .anyMatch(role -> role.getRole().
                                    equals("ADMINISTRATOR"));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public Long getUserIdFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        User user = findByUsername(username)
              .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        return user.getId();
    }

    @Override
    public boolean isAdmin(Authentication authentication) {
    Long id = getUserIdFromAuthentication(authentication);
    if (id == null) return false;
    return userRepo.findById(id)
        .map(this::isAdmin)    // reutiliza isAdmin(User user)
        .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getClientIdsForUser(Long userId) {
        if (userId == null) return Collections.emptyList();
        List<Long> ids = userRepo.findClientIdsByUserId(userId);
        return ids == null ? Collections.emptyList() : ids;
  }

}
