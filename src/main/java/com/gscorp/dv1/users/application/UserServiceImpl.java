package com.gscorp.dv1.users.application;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;


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
import com.gscorp.dv1.users.web.dto.InviteUserRequestWhatsApp;
import com.gscorp.dv1.users.web.dto.UserUpdateDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        //NO contraseña aun, ni token (El controller gestiona eso)
        user.setPassword(null);
        user.setInvitationToken(null);
        user.setInvitationTokenExpiry(null);

        // Asignar roles
        if (request.roleIds() != null) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : request.roleIds()) {
                    roleRepo.findById(roleId).ifPresent(roles::add);
            }
            user.setRoles(roles);
        }

        // Asignar clientes
        if (request.clientIds() != null) {
            Set<Client> clients = new HashSet<>();
            for (Long clientId : request.clientIds()) {
                clientRepo.findById(clientId).ifPresent(clients::add);
            }
            user.setClients(clients);
        }

        //Asociar empleado
        Employee employee = null;
        if(request.employeeId() != null) {
            employee = employeeRepo.findById(request.employeeId())
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

            if(employee.getUser() != null) {
                throw new IllegalArgumentException("Empleado ya asociado a un usuario");
            }

            // asignar en el owning side (User tiene @JoinColumn(employee_id))
            user.setEmployee(employee);
        }

        // persistir usuario (incluye employee_id si fue seteado)
        User savedUser;
        try {
            savedUser = userRepo.save(user);
        } catch (DataIntegrityViolationException ex) {
            // posible race condition si otro hilo asignó el mismo employee; manejar claramente
            throw new IllegalStateException("No se pudo crear el usuario: conflicto de integridad", ex);
        }

        // sincronizar el lado inverso en memoria para la misma transacción
        if (employee != null) {
            employee.setUser(savedUser);
            // no es necesario guardar employee porque employee es el lado inverso (la columna está en user)
        }

        return savedUser;
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

    /**
     * Intenta resolver y validar la zona del usuario registrada en la entidad User.
     * - Retorna Optional.empty() si userId es null, si no existe user o si la zona no está definida o es inválida.
     * - El resultado está cacheado por userId (cache "userZones") para reducir consultas.
     */
    @Override
    @Cacheable(value = "userZones", key = "#userId")
    public Optional<ZoneId> getUserZone(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        try {
            return userRepo.findById(userId)
                    .map(User::getTimeZone)         // ajusta si tu entidad usa otro nombre
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .flatMap(s -> {
                        try {
                            return Optional.of(ZoneId.of(s));
                        } catch (DateTimeException e) {
                            log.warn("Zona inválida almacenada para user {}: '{}'", userId, s);
                            return Optional.empty();
                        }
                    });
        } catch (Exception ex) {
            // no propagamos excepciones para que quien llama haga fallback; logueamos lo ocurrido
            log.error("Error leyendo zona para user {}: {}", userId, ex.getMessage(), ex);
            return Optional.empty();
        }
    }



    @Override
    @Transactional
    public Optional<User> updateUser(Long userId, UserUpdateDto dto) {
        if (userId == null) throw new IllegalArgumentException("userId es requerido");
        if (dto == null) throw new IllegalArgumentException("user update dto es requerido");

        Optional<User> optUser = userRepo.findById(userId);
        if (optUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optUser.get();

        // Campos simples
        if (dto.username() != null) {
            user.setUsername(dto.username().trim());
        }

        if (dto.mail() != null) {
            user.setMail(dto.mail().trim());
        }

        if (dto.active() != null) {
            user.setActive(dto.active());
        }

        // Asociaciones: roles
        if (dto.roleIds() != null) {
            Set<Role> roles = new HashSet<>();
            if (!dto.roleIds().isEmpty()) {
                Iterable<Role> found = roleRepo.findAllById(dto.roleIds());
                found.forEach(roles::add);
                // opcional: validar que se encontraron todas las ids solicitadas
                if (roles.size() != dto.roleIds().size()) {
                    log.warn("Algunas roleIds no existen al actualizar user {}: solicitado={}, encontrados={}", userId, dto.roleIds(), roles.size());
                    // Puedes optar por lanzar excepción en lugar de ignorar
                    // throw new EntityNotFoundException("Algunas roles no existen");
                }
            }
            user.setRoles(roles);
        }

        // Asociaciones: clients
        if (dto.clientIds() != null) {
            Set<Client> clients = new HashSet<>();
            if (!dto.clientIds().isEmpty()) {
                Iterable<Client> found = clientRepo.findAllById(dto.clientIds());
                found.forEach(clients::add);
                if (clients.size() != dto.clientIds().size()) {
                    log.warn("Algunas clientIds no existen al actualizar user {}: solicitado={}, encontrados={}", userId, dto.clientIds(), clients.size());
                    // opcional: lanzar excepción
                }
            }
            user.setClients(clients);
        }

        // Employee (one-to-one)
        if (dto.employeeId() != null) {
            Long empId = dto.employeeId();
            Optional<Employee> empOpt = employeeRepo.findById(empId);
            if (empOpt.isPresent()) {
                user.setEmployee(empOpt.get());
            } else {
                log.warn("Employee id {} no encontrado al actualizar usuario {}", empId, userId);
                // opcional: throw new EntityNotFoundException(...)
                user.setEmployee(null);
            }
        }

        // Time zone validation
        if (dto.timeZone() != null) {
            String tz = dto.timeZone().trim();
            if (tz.isEmpty()) {
                user.setTimeZone(null);
            } else {
                try {
                    ZoneId.of(tz); // valida
                    user.setTimeZone(tz);
                } catch (DateTimeException dex) {
                    throw new IllegalArgumentException("timeZone inválida: " + tz, dex);
                }
            }
        }

        // Persistir cambios
        User saved = userRepo.save(user);
        return Optional.of(saved);
    }



        /**
     * Crea un usuario invitado para envío vía WhatsApp.
     *
     * Validaciones y comportamiento:
     * - normaliza y valida phone a E.164 (usa libphonenumber)
     * - evita crear duplicados por teléfono o username
     * - asigna roles y clientes indicados si existen
     * - asocia empleado si se provee y no está ya asociado
     * - deja password null (el controller/flow debe crear token y definir contraseña)
     */
    @Override
    @Transactional
    public User createInvitedUserWhatsApp(InviteUserRequestWhatsApp request) {
        if (request == null) {
            throw new IllegalArgumentException("Request no puede ser null");
        }
        if (!StringUtils.hasText(request.username())) {
            throw new IllegalArgumentException("username es requerido");
        }
        // Normalizar teléfono a E.164
        String normalizedPhone = normalizeToE164(request.phone());
        if (normalizedPhone == null) {
            throw new IllegalArgumentException("Teléfono inválido. Debe estar en formato internacional (E.164).");
        }

        // Comprobar duplicados por teléfono
        Optional<User> existingByPhone = userRepo.findByPhone(normalizedPhone);
        if (existingByPhone.isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese teléfono");
        }

        // Comprobar duplicado por username
        Optional<User> existingByUsername = userRepo.findByUsername(request.username());
        if (existingByUsername.isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setMail(null); // no email para envíos WhatsApp
        user.setPhone(normalizedPhone);
        user.setActive(true); // o false según tu política; aquí lo dejamos activo pero sin password
        // marca canal preferido si tu entidad tiene el campo (opcional)
        // user.setPreferredContactChannel(ContactChannel.WHATSAPP);

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

        // Asociar empleado si se indica
        if (request.employeeId() != null) {
            Employee employee = employeeRepo.findById(request.employeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
            if (employee.getUser() != null) {
                throw new IllegalArgumentException("Empleado ya asociado a un usuario");
            }
            // asociar
            employeeRepo.findById(request.employeeId()).ifPresent(user::setEmployee);
        }

        // No establecer contraseña ni token en este punto.
        user.setPassword(null);

        // Guardar y devolver
        return userRepo.save(user);
    }

    private static final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();

    /**
     * Normaliza número a formato E.164 usando libphonenumber.
     * Devuelve null si no se puede parsear/validar.
     */
    private String normalizeToE164(String rawPhone) {
        if (!StringUtils.hasText(rawPhone)) return null;
        String s = rawPhone.trim();
        try {
            PhoneNumber pn = PHONE_UTIL.parse(s, null); // region null asume prefijo internacional
            if (!PHONE_UTIL.isValidNumber(pn)) {
                log.debug("Número no válido según libphonenumber: {}", rawPhone);
                return null;
            }
            return PHONE_UTIL.format(pn, PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            log.debug("Error parseando número '{}' : {}", rawPhone, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error inesperado parseando número '{}': {}", rawPhone, e.getMessage(), e);
            return null;
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findEmployeeIdByUserId(Long userId) {
        return userRepo.findEmployeeIdByUserId(userId);
    }

}
