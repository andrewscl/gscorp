package com.gscorp.dv1.employees.application;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import com.gscorp.dv1.employees.infrastructure.EmployeeEditProjection;
import com.gscorp.dv1.employees.infrastructure.EmployeeTableProjection;
import com.gscorp.dv1.employees.web.dto.CreateEmployeeRequest;
import com.gscorp.dv1.employees.web.dto.EmployeeEditDto;
import com.gscorp.dv1.employees.web.dto.EmployeeSelectDto;
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
    public EmployeeEditDto findByIdWithUserAndProjects(Long id) {
        EmployeeEditProjection projection = employeeRepository.findEmployeeProjectionById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Employee not found with id: " + id));
        return EmployeeEditDto.fromProjection(projection);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findProjectIdsByEmployeeId(Long employeeId) {
        return employeeRepository.findProjectIdsByEmployeeId(employeeId);
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
    public Page<EmployeeTableProjection> getEmployeeTable(
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
        return employeeRepository
                        .findTableRowsForClientIds(clientIds, safeQ, active, pg);
    }

}
