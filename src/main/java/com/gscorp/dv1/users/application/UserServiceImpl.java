package com.gscorp.dv1.users.application;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.admin.clients.infrastructure.Client;
import com.gscorp.dv1.admin.clients.infrastructure.ClientRepository;
import com.gscorp.dv1.admin.companies.infrastructure.Company;
import com.gscorp.dv1.admin.companies.infrastructure.CompanyRepository;
import com.gscorp.dv1.auth.application.PasswordResetTokenService;
import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;
import com.gscorp.dv1.hr.employees.infrastructure.EmployeeRepository;
import com.gscorp.dv1.roles.application.RoleService;
import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.infrastructure.UserRepository;
import com.gscorp.dv1.users.infrastructure.UserSpecRepository;
import com.gscorp.dv1.users.infrastructure.projections.UserTableProjection;
import com.gscorp.dv1.users.infrastructure.specification.UserSpecifications;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;
import com.gscorp.dv1.users.web.dto.UserTableDto;
import com.gscorp.dv1.users.web.dto.UserUpdateDto;
import com.gscorp.dv1.users.web.dto.UserViewDto;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepo;
    private final UserSpecRepository userSpecRepo;
    private final EmployeeRepository employeeRepository;
    private final RoleService roleService;
    private final PasswordEncoder encoder;
    private final PasswordResetTokenService passwordResetTokenService;

    private static final Duration INVITE_TTL = Duration.ofDays(7);

    @Transactional
    public Long createUser (CreateUserRequest req){
        if(req.username()==null || req.username().isBlank())
            throw new IllegalArgumentException("username requerido");
        if(req.password()==null || req.password().isBlank())
            throw new IllegalArgumentException("password requerido");
        if(userRepo.findByUsername(req.username()).isPresent())
            throw new DataIntegrityViolationException("El usuario ya existe");

        Role role = roleService.findById(req.roleId());

        var u = new User();
        u.setUsername(req.username());
        u.setMail(req.mail());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(role);
        u.setActive(true);
        u.setStatus(UserStatus.ACTIVE);
        u.setCompanies(new HashSet<>());
        u.setClients(new HashSet<>());

        User user = userRepo.save(u);
        assignMatrixAndValidateForCreate(user, req.employeeId(), req.companyIds(), req.clientIds());
        return user.getId();
    }

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

    @Transactional(readOnly = true)
    public List<User> findAll(){
        return userRepo.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id){
        return userRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public UserViewDto findWithRolesAndClientsById(Long id){
        User user = userRepo.findWithRolesAndClientsById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        return UserViewDto.from(user);
    }

    @Transactional(readOnly = true)
    public UserViewDto findWithCompaniesAndClientsByExternalId(UUID externalId){
        User user = userRepo.findWithCompaniesAndClientsByExternalId(externalId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + externalId));
        return UserViewDto.from(user);  
    }

    @Transactional(readOnly = true)
    public List<User> findAllWithCompaniesAndClients(){
        return userRepo.findAllWithCompaniesAndClients();
    }


    @Transactional
    public User createInvitedUser(InviteUserRequest request) {
        if(request.username()==null || request.username().isBlank())
            throw new IllegalArgumentException("username requerido");
        if(userRepo.findByUsername(request.username()).isPresent())
            throw new DataIntegrityViolationException("El usuario ya existe");

        Role role = roleService.findById(request.roleId());

        User user = new User();
        user.setUsername(request.username());
        user.setMail(request.mail());
        user.setRole(role);
        user.setPassword(null);
        user.setActive(false);
        user.setStatus(UserStatus.INVITED);
        user.setCompanies(new HashSet<>());
        user.setClients(new HashSet<>());

        assignMatrixAndValidateForCreate(user, request.employeeId(), request.companyIds(), request.clientIds());
        User savedUser = userRepo.save(user);
        passwordResetTokenService.createToken(savedUser, INVITE_TTL);
        return savedUser;
    }


    @Transactional(readOnly = true)
    public Boolean isInvitationTokenValid(String token) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenService.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        return passwordResetTokenService.isValid(tokenOpt.get());
    }

    @Transactional
    public Boolean setPasswordFromInvitation(String token, String password){

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenService.findByToken(token);
        if (tokenOpt.isEmpty()) return false;

        PasswordResetToken tokenEntity = tokenOpt.get();
        if (!passwordResetTokenService.isValid(tokenEntity)) return false;

        User user = tokenEntity.getUser();
        if (user == null) return false;

        //Establecer credenciales de seguridad y activación de la cuenta
        user.setPassword(encoder.encode(password));
        user.setActive(true);
        user.setStatus(UserStatus.ACTIVE);

        userRepo.save(user);
        passwordResetTokenService.markAsUsed(tokenEntity);

        return true;
    }

    @Transactional
    public void save(User user) {
        userRepo.save(user);
    }

    @Transactional(readOnly = true)
    public Boolean isAdmin(User user) {
        if(user == null || user.getRole() == null) return false; 
        return "ADMINISTRATOR".equalsIgnoreCase(user.getRole().getRole());
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return userRepo.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Long getUserIdFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        User user = findByUsername(username)
              .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        return user.getId();
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(Authentication authentication) {
    Long id = getUserIdFromAuthentication(authentication);
    if (id == null) return false;
    return userRepo.findById(id)
        .map(this::isAdmin)    // reutiliza isAdmin(User user)
        .orElse(false);
    }

    /**
     * Intenta resolver y validar la zona del usuario registrada en la entidad User.
     * - Retorna Optional.empty() si userId es null, si no existe user o si la zona no está definida o es inválida.
     * - El resultado está cacheado por userId (cache "userZones") para reducir consultas.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userZones", key = "#userId")
    public Optional<ZoneId> getUserZone(UUID externalId) {
        if (externalId == null) {
            return Optional.empty();
        }

        try {
            return userRepo.findByExternalId(externalId)
                    .map(user -> user.getTimeZone())         // ajusta si tu entidad usa otro nombre
                    .map(tz -> tz.trim())
                    .filter(s -> !s.isEmpty())
                    .flatMap(s -> {
                        try {
                            return Optional.of(ZoneId.of(s));
                        } catch (DateTimeException e) {
                            log.warn("Zona inválida almacenada para user {}: '{}'", externalId, s);
                            return Optional.empty();
                        }
                    });
        } catch (Exception ex) {
            // no propagamos excepciones para que quien llama haga fallback; logueamos lo ocurrido
            log.error("Error leyendo zona para user {}: {}", externalId, ex.getMessage(), ex);
            return Optional.empty();
        }
    }


    @Transactional
    public Optional<UserViewDto> updateUser(UUID userExternalId, UserUpdateDto dto) {
        if (userExternalId == null)
            throw new IllegalArgumentException("userExternalId es requerido");
        if (dto == null)
            throw new IllegalArgumentException("user update dto es requerido");

        Optional<User> optUser = userRepo.findByExternalId(userExternalId);
        if (optUser.isEmpty()) return Optional.empty();
        User user = optUser.get();

        Hibernate.initialize(user.getClients());
        Hibernate.initialize(user.getCompanies());
        Hibernate.initialize(user.getEmployee());
        Hibernate.initialize(user.getRole());

        if (dto.username() != null) user.setUsername(dto.username().trim());
        if (dto.mail() != null) user.setMail(dto.mail().trim());
        if (dto.active() != null) user.setActive(dto.active());
        if (dto.roleId() != null && !dto.roleId().equals(user.getRole().getId())) {
            Role newRole = roleService.findById(dto.roleId());
            user.setRole(newRole);
        }

        assignMatrixAndValidateForUpdate(user, dto.employeeId(), dto.companyIds(), dto.clientIds());
        User updatedUser = userRepo.save(user);

        return Optional.of(UserViewDto.from(updatedUser));
    }

    @Transactional(readOnly = true)
    public Optional<Long> findEmployeeIdByUserId(Long userId) {
        if (userId == null) return Optional.empty();
        return userRepo.findEmployeeIdByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<UserTableDto> getAllUsersWithEmployee(
        int page, int size
    ){

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200);

        PageRequest pg = PageRequest.of(safePage, safeSize);
        Page<UserTableProjection> projections;
        projections = userRepo.findAllUsersWithEmployee(pg);
        return projections.map(UserTableDto::fromProjection);
    }

    @Transactional(readOnly = true)
    public Page<UserTableDto> searchUsersWithEmployee(
        String q, UserStatus status, int page, int size
    ){

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200);
        String safeQ = (q == null || q.trim().isEmpty()) ? null : q.trim();

        PageRequest pg = PageRequest.of(safePage, safeSize);
        Specification<User> spec = UserSpecifications.searchUsers(safeQ, status);
        Page<User> usersPage = userSpecRepo.findAll(spec, pg);
        return usersPage.map(UserTableDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUsersStatistics () {
        return Map.of(
            "invitedUsers", userRepo.countByStatus(UserStatus.INVITED),            
            "activeUsers", userRepo.countByStatus(UserStatus.ACTIVE),
            "inactiveUsers", userRepo.countByStatus(UserStatus.INACTIVE),
            "expiredUsers", userRepo.countByStatus(UserStatus.EXPIRED),
            "suspendedUsers", userRepo.countByStatus(UserStatus.SUSPENDED)
        );
    }

    private void assignMatrixAndValidateForCreate(User user, Long employeeId, Set<Long> companyIds, Set<Long> clientIds){

        Set<Long> safeCompanyIds = companyIds != null ? companyIds : Set.of();
        Set<Long> safeClientIds = clientIds != null ? clientIds : Set.of();
        switch (user.getRole().getAccountType()) {
            case HOLDING -> {
                // Validación de consistencia: si se proporcionan IDs de empresa, deben existir todas
                if (!safeCompanyIds.isEmpty()) {
                    List<Company> companies = companyRepository.findAllById(safeCompanyIds);
                    if (companies.size() != safeCompanyIds.size()) {
                        throw new EntityNotFoundException("One or more company IDs are invalid");
                    }
                    user.getCompanies().addAll(companies);
                }
                // Gestión de empleado opcional
                if(employeeId != null) {
                    Employee employee = employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + employeeId));
                    if (employee.getUser() != null) {
                        throw new IllegalStateException("El empleado ya tiene un usuario asignado");
                    }
                    user.setEmployee(employee);
                    employee.setUser(user);
                }
            }
            case COMPANY -> {
                if(employeeId == null) {
                    throw new IllegalArgumentException("The accountType COMPANY must be associated with an employee");
                }
                if (safeCompanyIds.size() != 1)
                    throw new IllegalArgumentException("The accountType COMPANY must be associated with exactly one company");

                Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + employeeId));
                if (employee.getUser() != null) {
                    throw new IllegalStateException("El empleado ya tiene un usuario asignado");
                }
                user.setEmployee(employee);
                employee.setUser(user);

                List<Company> companies = companyRepository.findAllById(safeCompanyIds);
                if (companies.size() != 1) {
                    throw new EntityNotFoundException("The provided company ID is invalid");
                }
                user.getCompanies().addAll(companies);
                if (!safeClientIds.isEmpty()) {
                    List<Client> clients = clientRepository.findAllById(safeClientIds);
                    if (clients.size() != safeClientIds.size()) {
                        throw new EntityNotFoundException("One or more client IDs are invalid");
                    }
                    user.getClients().addAll(clients);
                    // Mantener consistencia bidireccional en memoria
                    for (Client client : clients) {
                        if (client.getUsers() == null) client.setUsers(new HashSet<>());
                        client.getUsers().add(user);
                    }
                }
            }

            case CLIENT -> {
                if(employeeId != null) {
                    throw new IllegalArgumentException("The accountType CLIENT cannot be associated with an employee");
                }
                if (!safeClientIds.isEmpty()) {
                    List<Client> clients = clientRepository.findAllById(safeClientIds);
                    if (clients.size() != safeClientIds.size()) {
                        throw new EntityNotFoundException("One or more client IDs are invalid");
                    }
                    user.getClients().addAll(clients);
                    // Mantener consistencia bidireccional en memoria
                    for (Client client : clients) {
                        if (client.getUsers() == null) client.setUsers(new HashSet<>());
                        client.getUsers().add(user);
                    }
                }
            }
        }
    }

    private void assignMatrixAndValidateForUpdate(User user, Long employeeId, Set<Long> companyIds, Set<Long> clientIds){

    // 💡 CONSOLIDACIÓN INTERNA: Si vienen nulos del DTO, la matriz usa los datos actuales de la entidad
    Set<Long> finalCompanyIds = (companyIds != null) ? companyIds : 
        user.getCompanies().stream().map(c -> c.getId()).collect(Collectors.toSet());
        
    Set<Long> finalClientIds = (clientIds != null) ? clientIds : 
        user.getClients().stream().map(c-> c.getId()).collect(Collectors.toSet());
        
    Long finalEmployeeId = (employeeId != null) ? employeeId : 
        (user.getEmployee() != null ? user.getEmployee().getId() : null);

        switch (user.getRole().getAccountType()) {
            case HOLDING -> {
                // Validación de consistencia: si se proporcionan IDs de empresa, deben existir todas
                if (companyIds != null) {
                    if (finalCompanyIds.isEmpty()) {
                        throw new IllegalArgumentException("The accountType HOLDING must be associated with at least one company");
                    }
                    List<Company> companies = companyRepository.findAllById(finalCompanyIds);
                    if (companies.size() != finalCompanyIds.size()) {
                        throw new EntityNotFoundException("One or more company IDs are invalid");
                    }
                    user.getCompanies().clear();
                    user.getCompanies().addAll(companies);
                } else {
                    // Si no vino el campo en el DTO, validamos que en la DB el usuario ya tenga al menos una empresa
                    if (user.getCompanies().isEmpty()) {
                        throw new IllegalArgumentException("The accountType HOLDING must be associated with at least one company");
                    }
                }

                // Gestión de empleado opcional
                if (user.getEmployee() == null) {
                    if(finalEmployeeId != null) {
                        Employee employee = employeeRepository.findById(finalEmployeeId)
                            .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + finalEmployeeId));
                        if (employee.getUser() != null && !employee.getUser().getId().equals(user.getId())) {
                            throw new IllegalStateException("El empleado ya tiene un usuario asignado");
                        }
                        user.setEmployee(employee);
                        employee.setUser(user);
                    }
                } else {
                    // Si ya tiene empleado asignado, no hacemos nada; se mantiene la relación existente
                    if (finalEmployeeId != null && !finalEmployeeId.equals(user.getEmployee().getId())) {
                        throw new IllegalArgumentException("The employee associated with this user cannot be changed");
                    }
                }
            }
            case COMPANY -> {
                // Gestión de Empleado (Obligatorio, pero inmutable si ya existe)
                if (user.getEmployee() == null) {
                    if(finalEmployeeId == null) {
                        throw new IllegalArgumentException("The accountType COMPANY must be associated with an employee");
                    }
                    Employee employee = employeeRepository.findById(finalEmployeeId)
                        .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + finalEmployeeId));
                    if (employee.getUser() != null && !employee.getUser().getId().equals(user.getId())) {
                        throw new IllegalStateException("El empleado ya tiene un usuario asignado");
                    }
                    user.setEmployee(employee);
                    employee.setUser(user);
                } else {
                    if (finalEmployeeId != null && !finalEmployeeId.equals(user.getEmployee().getId())) {
                        throw new IllegalArgumentException("The employee associated with this user cannot be changed");
                    }
                
                }
                // Actualización de Compañía: Si no se envía nada, no se toca la DB
                if (companyIds != null) {
                    if (finalCompanyIds.size() != 1) {
                        throw new IllegalArgumentException("The accountType COMPANY must be associated with exactly one company");
                    }
                    List<Company> companies = companyRepository.findAllById(finalCompanyIds);
                    if (companies.size() != 1) {
                        throw new EntityNotFoundException("The provided company ID is invalid");
                    }
                    user.getCompanies().clear();
                    user.getCompanies().addAll(companies);
                } else {
                    // Si no vino en el DTO, igual validamos que el usuario ya tenga una asignada en DB por consistencia
                    if (user.getCompanies().size() != 1) {
                        throw new IllegalArgumentException("The accountType COMPANY must have exactly one company");
                    }
                }
                

                // Actualización de Clientes
                if (clientIds!= null) {
                    List<Client> clients = clientRepository.findAllById(finalClientIds);
                    if (clients.size() != finalClientIds.size()) {
                        throw new EntityNotFoundException("One or more client IDs are invalid");
                    }
                    user.getClients().forEach(oldClient -> {
                        if(oldClient.getUsers() != null) oldClient.getUsers().remove(user);
                    });
                    
                    user.getClients().clear();
                    user.getClients().addAll(clients);
                    // Mantener consistencia bidireccional en memoria
                    for (Client client : clients) {
                        if (client.getUsers() == null) client.setUsers(new HashSet<>());
                        client.getUsers().add(user);
                    }
                }
            }

            case CLIENT -> {
                if(finalEmployeeId != null) {
                    throw new IllegalArgumentException("The accountType CLIENT cannot be associated with an employee");
                }
                if (clientIds != null) {
                    List<Client> clients = clientRepository.findAllById(finalClientIds);
                    if (clients.size() != finalClientIds.size()) {
                        throw new EntityNotFoundException("One or more client IDs are invalid");
                    }
                    user.getClients().forEach(oldClient -> {
                        if(oldClient.getUsers() != null) oldClient.getUsers().remove(user);
                    });
                    user.getClients().clear();
                    user.getClients().addAll(clients);
                    // Mantener consistencia bidireccional en memoria
                    for (Client client : clients) {
                        if (client.getUsers() == null) client.setUsers(new HashSet<>());
                        client.getUsers().add(user);
                    }
                }
                if(!user.getCompanies().isEmpty()){
                    user.getCompanies().clear();
                }
            }
        }
    }


    @Transactional(readOnly = true)
    public Optional<User> findByExternalId(UUID externalId) {
        return userRepo.findByExternalId(externalId);
    }

}
