package com.gscorp.dv1.employees.application;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.bank.application.BankService;
import com.gscorp.dv1.bank.infrastructure.Bank;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.infrastructure.EmployeeRepository;
import com.gscorp.dv1.employees.infrastructure.EmployeeSelectProjection;
import com.gscorp.dv1.employees.infrastructure.EmployeeEditProjection;
import com.gscorp.dv1.employees.infrastructure.EmployeeTableProjection;
import com.gscorp.dv1.employees.infrastructure.EmployeeViewProjection;
import com.gscorp.dv1.employees.web.dto.CreateEmployeeRequest;
import com.gscorp.dv1.employees.web.dto.EmployeeEditDto;
import com.gscorp.dv1.employees.web.dto.EmployeeSelectDto;
import com.gscorp.dv1.employees.web.dto.EmployeeTableDto;
import com.gscorp.dv1.employees.web.dto.EmployeeViewDto;
import com.gscorp.dv1.employees.web.dto.UpdateEmployeeRequest;
import com.gscorp.dv1.nationalities.application.NationalityService;
import com.gscorp.dv1.nationalities.infrastructure.Nationality;
import com.gscorp.dv1.positions.application.PositionService;
import com.gscorp.dv1.positions.infrastructure.Position;
import com.gscorp.dv1.professions.application.ProfessionService;
import com.gscorp.dv1.professions.infrastructure.Profession;
import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.shiftpatterns.application.ShiftPatternService;
import com.gscorp.dv1.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.users.application.UserService;

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
    private final UserService userService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public List<Employee> findAll (){
        return employeeRepository.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    public EmployeeEditDto findByIdEditEmployee(Long id) {
        EmployeeEditProjection projection = employeeRepository.findEmployeeEditProjectionById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Employee not found with id: " + id));
        return EmployeeEditDto.fromProjection(projection);
    }


    @Override
    @Transactional(readOnly = true)
    public EmployeeViewDto findByIdViewEmployee(Long id) {
        EmployeeViewProjection projection = employeeRepository.findEmployeeViewProjectionById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Employee not found with id: " + id));
        return EmployeeViewDto.fromProjection(projection);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Long> findProjectIdsByEmployeeId(Long employeeId) {
        return employeeRepository.findProjectIdsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findProjectNamesByEmployeeId(Long employeeId) {
        return employeeRepository.findProjectNamesByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findProfessionIdsByEmployeeId(Long employeeId) {
        return employeeRepository.findProfessionIdsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findProfessionNamesByEmployeeId(Long employeeId) {
        return employeeRepository.findProfessionNamesByEmployeeId(employeeId);
    }


    @Override
    @Transactional
    public Employee saveEmployee (Employee employee){
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    @Transactional
    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUserUsername(username);
    }

    @Override
    @Transactional
    public Employee createEmployeeFromRequest(CreateEmployeeRequest req) {
        
        //Establecer proyecto

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
                .active(true)
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

    @Override
    @Transactional
    public List<Employee> findAllWithProjects() {
        return employeeRepository.findAllWithProjects();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findAllUnassignedEmployees() {
        return employeeRepository.findAllUnassignedEmployees();
    }



    @Override
    @Transactional(readOnly = true)
    public List<Employee> findAllWithUserAndProjectsAndPosition() {
        return employeeRepository.findAllWithUserAndProjectsAndPosition();
    }



    @Override
    @Transactional(readOnly = true)
    public EmployeeSelectDto findEmployeeByUserId(Long userId) {
        return employeeRepository.findByUser_Id(userId)
                .map(EmployeeSelectDto::fromProjection)
                .orElse(null);
    }



    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSelectDto> getAllEmployeesSelectDto() {
        List<EmployeeSelectProjection> projections = employeeRepository.findAllProjections();
        return projections.stream()
                .map(EmployeeSelectDto::fromProjection)
                .toList();
    }



    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeTableDto> getEmployeeTable(
                Long userId, String q, Boolean active, int page, int size) {

        // Normalizar page/size (Spring Data usa 0-based)
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200); // límites: min 5, max 200

        // Normalizar query
        String safeQ = (q == null || q.trim().isEmpty()) ? null : q.trim();

        // Resolver clientes asociados al usuario
        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
        return Page.empty();
        }

        PageRequest pg = PageRequest.of(safePage, safeSize);

        Page<EmployeeTableProjection> projectionPage =
                    employeeRepository
                        .findTableRowsForClientIds(clientIds, safeQ, active, pg);

        return projectionPage.map(EmployeeTableDto::fromProjection);

    }


    @Override
    @Transactional
    public Optional<EmployeeViewDto> updateEmployee(Long id, UpdateEmployeeRequest req) {
        
        if (id == null) {
            throw new IllegalArgumentException("El ID del empleado es requerido para la actualización.");
        }

        // Cargar la entidad existente
        Employee entity = employeeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró el empleado con ID: " + id));

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
                photoUrl = "/files/employee_photos/photos/  " + storedFilename;
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

        // Guardar cambiosen la base de datos
        Employee updatedEmployee = employeeRepository.save(entity);

        Optional<EmployeeViewProjection> projectionOpt = employeeRepository
                                            .findEmployeeViewProjectionById(updatedEmployee.getId());
        if (projectionOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(EmployeeViewDto.fromProjection(projectionOpt.get()));
    }

}
