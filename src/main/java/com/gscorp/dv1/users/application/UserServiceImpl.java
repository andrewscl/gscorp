package com.gscorp.dv1.users.application;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.auth.application.PasswordResetTokenService;
import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientRepository;
import com.gscorp.dv1.companies.infrastructure.Company;
import com.gscorp.dv1.companies.infrastructure.CompanyRepository;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.infrastructure.EmployeeRepository;
import com.gscorp.dv1.enums.AccountType;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.roles.application.RoleService;
import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.infrastructure.UserRepository;
import com.gscorp.dv1.users.infrastructure.UserSpecRepository;
import com.gscorp.dv1.users.infrastructure.UserTableProjection;
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

    @Autowired
    private final UserRepository userRepo;

    @Autowired
    private final UserSpecRepository userSpecRepo;

    @Autowired
    private final EmployeeRepository employeeRepository;

    private final RoleService roleService;
    private final PasswordEncoder encoder;

    @Autowired
    private final PasswordResetTokenService passwordResetTokenService;

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

        assignMatrixAndValidate(user, req.employeeId(), req.companyIds(), req.clientIds());

        return user.getId();
    }

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
    @Transactional(readOnly = true)
    public List<User> findAll(){
        return userRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id){
        return userRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserViewDto findWithRolesAndClientsById(Long id){
        User user = userRepo.findWithRolesAndClientsById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        return UserViewDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllWithCompaniesAndClients(){
        return userRepo.findAllWithCompaniesAndClients();
    }

    @Override
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

        User savedUser = userRepo.save(user);

        assignMatrixAndValidate(savedUser, request.employeeId(), request.companyIds(), request.clientIds());

        passwordResetTokenService.createToken(savedUser, Duration.ofDays(7));

        return savedUser;
    }


    @Override
    public Boolean isInvitationTokenValid(String token) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenService.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        return passwordResetTokenService.isValid(tokenOpt.get());
    }

    @Override
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

    @Override
    @Transactional
    public void save(User user) {
        userRepo.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isAdmin(User user) {
        if(user == null || user.getRole() == null) return false; 
        return "ADMINISTRATOR".equalsIgnoreCase(user.getRole().getRole());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return userRepo.findByUsername(username);
    }


    @Override
    @Transactional(readOnly = true)
    public Long getUserIdFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        User user = findByUsername(username)
              .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        return user.getId();
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
        if (optUser.isEmpty()) return Optional.empty();

        User user = optUser.get();

        // Inicializa las colecciones que podrían ser usadas fuera del contexto
        Hibernate.initialize(user.getClients());
        Hibernate.initialize(user.getCompanies());

        if (dto.username() != null) user.setUsername(dto.username().trim());
        if (dto.mail() != null) user.setMail(dto.mail().trim());
        if (dto.active() != null) user.setActive(dto.active());

        if (dto.roleId() != null && !dto.roleId().equals(user.getRole().getId())) {
            Role newRole = roleService.findById(dto.roleId());
            user.setRole(newRole);

        user.getCompanies().clear();
        user.getClients().clear();

        assignMatrixAndValidate(user, dto.employeeId(), dto.companyIds(), dto.clientIds());

        } else {

            if(dto.companyIds() != null) {
                List<Company> companies = companyRepository.findAllById(dto.companyIds());
                if (companies.size() != dto.companyIds().size()) {
                    throw new EntityNotFoundException("One or more company IDs are invalid");
                }
                user.getCompanies().clear();
                user.getCompanies().addAll(companies);
            }
            if(dto.clientIds() != null) {
                List<Client> clients = clientRepository.findAllById(dto.clientIds());
                if (clients.size() != dto.clientIds().size()) {
                    throw new EntityNotFoundException("One or more company IDs are invalid");
                }

                user.getClients().clear();
                user.getClients().addAll(clients);
            }
            if(dto.employeeId() != null && AccountType.COMPANY.equals(user.getRole().getAccountType())) {

                //Asignar y guardar empleado
                Employee employee = employeeRepository.findById(dto.employeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + dto.employeeId()));
                if (employee.getUser() != null) {
                    throw new IllegalStateException("El empleado ya tiene un usuario asignado");
                }
                employee.setUser(user);
                employeeRepository.save(employee);

            }
        }

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

        return Optional.of(userRepo.save(user));
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findEmployeeIdByUserId(Long userId) {
        if (userId == null) return Optional.empty();
        return userRepo.findEmployeeIdByUserId(userId);
    }


    @Override
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


    @Override
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


    @Override
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


    private void assignMatrixAndValidate(User user, Long employeeId, Set<Long> companyIds, Set<Long> clientIds){
        switch (user.getRole().getAccountType()) {
            case HOLDING -> {
                if(employeeId != null) {
                    throw new IllegalArgumentException("The accountType HOLDING cannot be associated with an employee");
                }
                List<Company> companies = companyRepository.findAllById(companyIds);
                if (companies.size() != companyIds.size()) {
                    throw new EntityNotFoundException("One or more company IDs are invalid");
                }
                user.getCompanies().addAll(companies);
            }
            case COMPANY -> {
                if(employeeId == null) {
                    throw new IllegalArgumentException("The accountType COMPANY must be associated with an employee");
                }
                List<Company> companies = companyRepository.findAllById(companyIds);
                if (companies.size() != companyIds.size()) {
                    throw new EntityNotFoundException("One or more company IDs are invalid");
                }
                user.getCompanies().addAll(companies);

                //Asignar y guardar empleado
                Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + employeeId));
                if (employee.getUser() != null) {
                    throw new IllegalStateException("El empleado ya tiene un usuario asignado");
                }
                employee.setUser(user);
                employeeRepository.save(employee);

            }
            case CLIENT -> {
                if(employeeId != null) {
                    throw new IllegalArgumentException("The accountType CLIENT cannot be associated with an employee");
                }
                List<Client> clients = clientRepository.findAllById(clientIds);
                if (clients.size() != clientIds.size()) {
                    throw new EntityNotFoundException("One or more company IDs are invalid");
                }

                user.getClients().addAll(clients);
            }
        }
    }


}
