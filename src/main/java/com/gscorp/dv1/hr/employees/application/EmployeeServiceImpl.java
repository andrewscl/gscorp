package com.gscorp.dv1.hr.employees.application;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.admin.clients.web.dto.ClientSelectDto;
import com.gscorp.dv1.admin.companies.infrastructure.Company;
import com.gscorp.dv1.admin.companies.infrastructure.CompanyRepository;
import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.admin.projects.infrastructure.Project;
import com.gscorp.dv1.core.bank.application.BankService;
import com.gscorp.dv1.core.bank.infrastructure.Bank;
import com.gscorp.dv1.core.nationalities.application.NationalityService;
import com.gscorp.dv1.core.nationalities.infrastructure.Nationality;
import com.gscorp.dv1.core.positions.application.PositionService;
import com.gscorp.dv1.core.positions.infrastructure.Position;
import com.gscorp.dv1.core.professions.application.ProfessionService;
import com.gscorp.dv1.core.professions.infrastructure.Profession;
import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;
import com.gscorp.dv1.hr.employees.infrastructure.EmployeeRepository;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeEditProjection;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeSelectProjection;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeTableProjection;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeViewProjection;
import com.gscorp.dv1.hr.employees.web.dto.EmployeeCreateUserDto;
import com.gscorp.dv1.hr.employees.web.dto.EmployeeEditDto;
import com.gscorp.dv1.hr.employees.web.dto.EmployeeSelectDto;
import com.gscorp.dv1.hr.employees.web.dto.EmployeeTableDto;
import com.gscorp.dv1.hr.employees.web.dto.EmployeeViewDto;
import com.gscorp.dv1.hr.employees.web.dto.request.CreateEmployeeRequest;
import com.gscorp.dv1.hr.employees.web.dto.request.UpdateEmployeeRequest;
import com.gscorp.dv1.operations.shiftpatterns.application.ShiftPatternService;
import com.gscorp.dv1.operations.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final ProjectService projectService;
    private final BankService bankService;
    private final NationalityService nationalityService;
    private final ProfessionService professionService;
    private final PositionService positionService;
    private final ShiftPatternService shiftPatternService;
    private final ClientService clientService;
    private final CompanyRepository companyRepo;

    @Value("${file.upload-dir}")
    private String uploadDir;


    @Transactional(readOnly = true)
    public List<Employee> findAll (){
        return employeeRepository.findAll();
    }


    @Transactional(readOnly = true)
    public EmployeeEditDto findByExternalIdEditEmployee(UUID externalId) {
        EmployeeEditProjection projection = employeeRepository.findEmployeeEditProjectionByExternalId(externalId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Employee not found with id: " + externalId));
        return EmployeeEditDto.fromProjection(projection);
    }


    @Transactional(readOnly = true)
    public EmployeeViewDto findByExternalIdViewEmployee(UUID externalId) {
        EmployeeViewProjection projection = employeeRepository.findEmployeeViewProjectionByExternalId(externalId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Employee not found with id: " + externalId));
        return EmployeeViewDto.fromProjection(projection);
    }


    @Transactional(readOnly = true)
    public EmployeeViewDto findByIdViewEmployee(Long id) {
        EmployeeViewProjection projection = employeeRepository.findEmployeeViewProjectionById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Employee not found with id: " + id));
        return EmployeeViewDto.fromProjection(projection);
    }


    @Transactional
    public Employee saveEmployee (Employee employee){
        return employeeRepository.save(employee);
    }


    @Transactional(readOnly = true)
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }


    @Transactional(readOnly = true)
    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUserUsername(username);
    }


    @Transactional
    public Employee createEmployeeFromRequest(CreateEmployeeRequest req) {
        
        Company companyReference = null;
        if (req.getCompanyId() != null) {
            // 🟢 No hace un SELECT a la base de datos. Crea un proxy ligero con el ID.
            companyReference = companyRepo.getReferenceById(req.getCompanyId());
        }

        Set<Project> projects =
            (req.getProjectIds() != null && !req.getProjectIds().isEmpty())
            ? new HashSet<>(projectService.findEntitiesById(req.getProjectIds()))
            : Set.of();
            
        
        Bank bank =
            (req.getBankId() != null)
            ? bankService.findById(req.getBankId())
            : null;

        Nationality nationality =
            (req.getNationalityId() != null)
            ? nationalityService.findById(req.getNationalityId())
            : null;

        Set<Profession> professions =
            (req.getProfessionIds() != null && !req.getProfessionIds().isEmpty())
            ? new HashSet<>(professionService.findAllById(req.getProfessionIds()))
            : Set.of();

        Position position =
            (req.getPositionId() != null)
            ? positionService.findById(req.getPositionId())
            : null;

        ShiftPattern shiftPattern =
            (req.getShiftPatternId() != null)
            ? shiftPatternService.findById(req.getShiftPatternId())
            : null;

        //Gestion de archivo
        String photoUrl = null;
        MultipartFile photo = req.getPhoto();
        if (photo != null && !photo.isEmpty()) {

            try{
                String extension = getExtension(photo.getOriginalFilename());
                String fileName =
                        "employee_photo_" +
                        System.currentTimeMillis() +
                        "_" +
                        (req.getRut() != null ? req.getRut() : "no_rut") +
                        "." +
                        (extension != null ? extension : "jpg");
                File destDir = new File(uploadDir);
                if (!destDir.exists()) destDir.mkdirs();
                File dest = new File(destDir, fileName);
                photo.transferTo(dest);
                //Guarda solo la ruta relativa
                photoUrl = uploadDir + "/" + fileName;
                
            } catch (IOException e){
                throw new RuntimeException("Error al guardar la fotografia", e);
            }
        
        }

        Employee entity = Employee.builder()
                .name(req.getName().trim())
                .fatherSurname(req.getFatherSurname())
                .motherSurname(req.getMotherSurname())
                .rut(req.getRut())
                .mail(req.getMail())
                .phone(req.getPhone())
                .secondaryPhone(req.getSecondaryPhone())
                .gender(req.getGender())
                .nationality(nationality)
                .maritalStatus(req.getMaritalStatus())
                .studyLevel(req.getStudyLevel())
                .professions(professions)
                .previtionalSystem(req.getPrevitionalSystem())
                .pensionEntity(req.getPensionEntity())
                .healthSystem(req.getHealthSystem())
                .healthEntity(req.getHealthEntity())
                .paymentMethod(req.getPaymentMethod())
                .bank(bank)
                .bankAccountType(req.getBankAccountType())
                .bankAccountNumber(req.getBankAccountNumber())
                .contractType(req.getContractType())
                .workSchedule(req.getWorkSchedule())
                .shiftSystem(req.getShiftSystem())
                .shiftPattern(shiftPattern)
                .position(position)
                .photoUrl(photoUrl)
                .hireDate(req.getHireDate())
                .birthDate(req.getBirthDate())
                .address(req.getAddress())
                .projects(projects)
                .company(companyReference)
                .active(true)
                .status(EmployeeStatus.ACTIVE)
                .build();

        Employee savedEmployee = employeeRepository.save(entity);

        //Asignar los proyectos el empleado (relacion inversa)
        for (Project p : projects) {
            System.out.println("Project: " + p.getId());
            p.getEmployees().add(savedEmployee);
        }

        return employeeRepository.save(savedEmployee);

    }


    private String getExtension(String filename) {
        if (filename == null) return null;

        int lastDot = filename.lastIndexOf('.');
        
        if (lastDot >=0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return null;
    }


    @Transactional(readOnly = true)
    public List<Employee> findAllWithProjects() {
        return employeeRepository.findAllWithProjects();
    }

    @Transactional(readOnly = true)
    public List<Employee> findAllUnassignedEmployees() {
        return employeeRepository.findAllUnassignedEmployees();
    }


    @Transactional(readOnly = true)
    public List<Employee> findAllWithUserAndProjectsAndPosition() {
        return employeeRepository.findAllWithUserAndProjectsAndPosition();
    }


    @Transactional(readOnly = true)
    public EmployeeSelectDto findEmployeeByUserId(Long userId) {
        return employeeRepository.findByUser_Id(userId)
                .map(EmployeeSelectDto::fromProjection)
                .orElseThrow(() -> new EntityNotFoundException(
            "No se encontró un empleado activo vinculado al usuario autenticado."
                ));
    }


    @Transactional(readOnly = true)
    public EmployeeSelectDto findEmployeeByUserExternalId(UUID userExternalId) {
        return employeeRepository.findByUserExternalId(userExternalId)
                .map(EmployeeSelectDto::fromProjection)
                .orElseThrow(() -> new EntityNotFoundException(
            "No se encontró un empleado activo vinculado al usuario autenticado."
                ));
    }


    @Transactional(readOnly = true)
    public List<EmployeeSelectDto> getAllEmployeesSelectDto() {
        List<EmployeeSelectProjection> projections = employeeRepository.findAllProjections();
        return projections.stream()
                .map(EmployeeSelectDto::fromProjection)
                .toList();
    }


    @Transactional(readOnly = true)
    public Page<EmployeeTableDto> getEmployeeTable(
                UUID userExternalId,
                String q,
                EmployeeStatus status,
                String userStatusStr,
                int page,
                int size) {

        UserStatus userStatusEnum = null;
        boolean showNotInvited = false;

        if (userStatusStr != null && !userStatusStr.isBlank()) {
            if ("NOT_INVITED".equals(userStatusStr)) {
                showNotInvited = true;
            } else {
                userStatusEnum = UserStatus.valueOf(userStatusStr);
            }
        }

        // Normalizar page/size (Spring Data usa 0-based)
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200); // límites: min 5, max 200

        String cleanQ = (q == null || q.trim().isEmpty()) ? null : q.trim();
        String repoQ = (cleanQ != null) ? "%" + cleanQ.toLowerCase() + "%" : null;

        // Resolver clientes asociados al usuario
        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
        return Page.empty();
        }

        PageRequest pg = PageRequest.of(safePage, safeSize);

        Page<EmployeeTableProjection> projectionPage =
                    employeeRepository
                        .findTableRowsForClientIds(clientIds, repoQ, status, userStatusEnum, showNotInvited, pg);

        return projectionPage.map(EmployeeTableDto::fromProjection);
    }


    @Transactional
    public Optional<EmployeeViewDto> updateEmployee(
                                        UUID externalId, UpdateEmployeeRequest req) {
        
        if (externalId == null) {
            throw new IllegalArgumentException("El ID del empleado es requerido para la actualización.");
        }

        // Cargar la entidad existente
        Employee entity = employeeRepository.findByExternalId(externalId)
            .orElseThrow(
                () -> new
                    EntityNotFoundException("No se encontró el empleado con ID: " + externalId));

        // Inicializar relaciones perezosas (lazy)
        Hibernate.initialize(entity.getNationality());
        Hibernate.initialize(entity.getBank());
        Hibernate.initialize(entity.getShiftPattern());
        Hibernate.initialize(entity.getPosition());
        Hibernate.initialize(entity.getProjects());
        Hibernate.initialize(entity.getProfessions());
        Hibernate.initialize(entity.getUser());

        // Actualizar los valores del empleado existente
        entity.setName(req.getName().trim());
        entity.setFatherSurname(req.getFatherSurname());
        entity.setMotherSurname(req.getMotherSurname());
        entity.setRut(req.getRut());
        entity.setMail(req.getMail());
        entity.setPhone(req.getPhone());
        entity.setSecondaryPhone(req.getSecondaryPhone());
        entity.setGender(req.getGender());
        
        // Actualización de relaciones con entidades externas
        entity.setNationality(req.getNationalityId() != null ? nationalityService.findById(req.getNationalityId()) : null);

        // Actualizar profesiones
        if (req.getProfessionIds() != null) {
            Set<Profession> newProfessions = new HashSet<>(professionService.findAllById(req.getProfessionIds()));

            //Quitar profesiones que ya no están
            entity.getProfessions().removeIf(profession -> !newProfessions.contains(profession));

            //Agregar nuevas relaciones
            for (Profession newProfession : newProfessions) {
                if (!entity.getProfessions().contains(newProfession)) {
                    entity.getProfessions().add(newProfession);
                }
            }
        }

        entity.setBank(req.getBankId() != null ? bankService.findById(req.getBankId()) : null);
        entity.setPosition(req.getPositionId() != null ? positionService.findById(req.getPositionId()) : null);
        entity.setShiftPattern(req.getShiftPatternId() != null ? shiftPatternService.findById(req.getShiftPatternId()) : null);

        entity.setMaritalStatus(req.getMaritalStatus());
        entity.setStudyLevel(req.getStudyLevel());
        entity.setPrevitionalSystem(req.getPrevitionalSystem());
        entity.setPensionEntity(req.getPensionEntity());
        entity.setHealthSystem(req.getHealthSystem());
        entity.setHealthEntity(req.getHealthEntity());
        entity.setPaymentMethod(req.getPaymentMethod());
        entity.setBankAccountType(req.getBankAccountType());
        entity.setBankAccountNumber(req.getBankAccountNumber());
        entity.setContractType(req.getContractType());
        entity.setWorkSchedule(req.getWorkSchedule());
        entity.setShiftSystem(req.getShiftSystem());
        entity.setHireDate(req.getHireDate());
        entity.setBirthDate(req.getBirthDate());
        entity.setAddress(req.getAddress());
        entity.setActive(true);


        String photoUrl = null;
        try {
            //Fotografias
            MultipartFile photo = req.getPhoto();
            if(photo != null && !photo.isEmpty()) {
                // Generar el nombre del archivo
                String originalFilename = photo.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                }
                String storedFilename = UUID.randomUUID().toString() + fileExtension;

                //Directorio fisico donde se guardara el archivo
                File dest = new File(uploadDir, "photos");
                if(!dest.exists()) dest.mkdirs();
                File storedFile = new File(dest, storedFilename);
                photo.transferTo(storedFile);
                photoUrl = "/files/employee_photos/photos/" + storedFilename;
            }

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar los archivos multimedia", e);
        }

        //Actualiza la fotografia solo si se envio una nueva
        if(photoUrl != null){
        entity.setPhotoUrl(photoUrl);
        }

        // Actualizar projects
        if (req.getProjectIds() != null) {
            Set<Project> newProjects = new HashSet<>(projectService.findEntitiesById(req.getProjectIds()));

            //Quitar proyectos que ya no están
            entity.getProjects().removeIf(project -> !newProjects.contains(project));

            //Agregar nuevas relaciones
            for (Project newProject : newProjects) {
                if (!entity.getProjects().contains(newProject)) {
                    entity.getProjects().add(newProject);
                }
            }
        }

        // Guardar cambios en la base de datos
        Employee updatedEmployee = employeeRepository.save(entity);

        Optional<EmployeeViewProjection> projectionOpt = employeeRepository
                                            .findEmployeeViewProjectionByExternalId(updatedEmployee.getExternalId());
        if (projectionOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(EmployeeViewDto.fromProjection(projectionOpt.get()));
    }

    @Transactional
    public EmployeeSelectDto findEmployeeSelectDtoById(Long id){
        return employeeRepository.findEmployeeSelectDtoById(id)
                .map(EmployeeSelectDto::fromProjection)
                .orElseThrow(
                    () -> new
                        EntityNotFoundException("Empleado no encontrado con id: " + id));
    }

    @Transactional
    public Employee validateAndAssignUser (Long employeeId, User user) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + employeeId));
        if (employee.getUser() != null) {
            throw new IllegalStateException("El empleado ya tiene un usuario asignado");
        }
        employee.setUser(user);
        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeSelectDto> findByStatus(EmployeeStatus status) {
        List<EmployeeSelectProjection> projections = employeeRepository.findByStatus(status);
        return projections.stream()
                .map(EmployeeSelectDto::fromProjection)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeCreateUserDto findDataForInvitation(UUID externalId) {
        Employee employee = employeeRepository.findForInvitationByExternalId(externalId)
            .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado"));

        Set<ClientSelectDto> clientDtos = employee.getProjects().stream()
                .map(project -> project != null ? project.getClient() : null)
                .filter(Objects::nonNull)
                .map(client -> new ClientSelectDto(client.getId(), client.getName()))
                .collect(Collectors.toSet());

        String username =
            ("" + employee.getName().charAt(0) + employee.getFatherSurname()).toLowerCase();

        return new EmployeeCreateUserDto(
                    employee.getId(),
                    employee.getExternalId(),
                    employee.getName(),
                    employee.getFatherSurname(),
                    employee.getMotherSurname(),
                    employee.getMail(),
                    username,
                    employee.getCompany() != null ? employee.getCompany().getId() : null,
                    clientDtos
                    );
    }

}