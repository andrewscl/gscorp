package com.gscorp.dv1.employees.web;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.bank.application.BankService;
import com.gscorp.dv1.bank.infrastructure.Bank;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.web.dto.CreateEmployeeRequest;
import com.gscorp.dv1.employees.web.dto.EmployeeDto;
import com.gscorp.dv1.nationalities.application.NationalityService;
import com.gscorp.dv1.nationalities.infrastructure.Nationality;
import com.gscorp.dv1.positions.application.PositionService;
import com.gscorp.dv1.positions.infrastructure.Position;
import com.gscorp.dv1.positions.web.dto.PositionDto;
import com.gscorp.dv1.professions.application.ProfessionService;
import com.gscorp.dv1.professions.infrastructure.Profession;
import com.gscorp.dv1.professions.web.dto.ProfessionDto;
import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.projects.web.dto.ProjectDto;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.UserDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeRestController {

    private final EmployeeService employeeService;
    private final UserService userService;
    private final ProjectService projectService;
    private final BankService bankService;
    private final NationalityService nationalityService;
    private final ProfessionService professionService;
    private final PositionService positionService;

    @PostMapping("/create")
    public ResponseEntity<EmployeeDto> createEmployee(
        @jakarta.validation.Valid @RequestBody CreateEmployeeRequest req,
        UriComponentsBuilder ucb
    ) {
        // Obtener usuario asociado si el request trae el ID
        User user = (req.userId() != null) ? userService.findById(req.userId()) : null;

        // Obtener proyectos asociados si el request trae los IDs
        Set<Project> projects = (req.projectIds() != null && !req.projectIds().isEmpty())
            ? projectService.findAllById(req.projectIds()).stream().collect(Collectors.toSet())
            : Set.of();

        // *** Busca el objeto Bank usando el bancoId del request ***
        Bank bank = (req.bankId() != null) ? bankService.findById(req.bankId()) : null;

        // *** Busca el objeto Nationality usando el nationalityId del request ***
        Nationality nationality = (req.nationalityId() != null) ? nationalityService.findById(req.nationalityId()) : null;

        Set<Profession> professions = (req.professionIds() != null && !req.professionIds().isEmpty())
            ? professionService.findAllById(req.professionIds()).stream().collect(Collectors.toSet())
            : Set.of();

        // *** Busca el objeto Position usando el positionId del request ***
        Position position = (req.positionId() != null) ? positionService.findById(req.positionId()) : null;

        var entity = Employee.builder()
            .name(req.name().trim())
            .fatherSurname(req.fatherSurname())
            .motherSurname(req.motherSurname())
            .rut(req.rut())
            .mail(req.mail())
            .phone(req.phone())
            .secondaryPhone(req.secondaryPhone())
            .gender(req.gender())
            .nationality(nationality)
            .maritalStatus(req.maritalStatus())
            .studyLevel(req.studyLevel())
            .professions(professions)
            .previtionalSystem(req.previtionalSystem())
            .healthSystem(req.healthSystem())
            .paymentMethod(req.paymentMethod())
            .bank(bank)
            .bankAccountType(req.bankAccountType())
            .bankAccountNumber(req.bankAccountNumber())
            .contractType(req.contractType())
            .workSchedule(req.workSchedule())
            .shiftSystem(req.shiftSystem())
            .shiftPattern(req.shiftPattern())
            .position(position)
            .password(req.password())
            .photoUrl(req.photoUrl())
            .hireDate(req.hireDate())
            .birthDate(req.birthDate())
            .exitDate(req.exitDate())
            .active(Boolean.TRUE.equals(req.active()))
            .address(req.address())
            .user(user)
            .projects(projects)
            .build();

        var saved = employeeService.saveEmployee(entity);
        var location = ucb.path("/api/employees/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = new EmployeeDto(
            saved.getId(),
            saved.getName(),
            saved.getMotherSurname(),
            saved.getFatherSurname(),
            saved.getRut(),
            saved.getMail(),
            saved.getPhone(),
            saved.getSecondaryPhone(),
            saved.getGender(),
            saved.getNationality() != null ? saved.getNationality().getId() : null,   // nationalityId
            saved.getNationality() != null ? saved.getNationality().getName() : null, // nationalityName
            saved.getMaritalStatus(),
            saved.getStudyLevel(),
            (saved.getProfessions() == null ? Set.<Profession>of() : saved.getProfessions())
                .stream()
                .map(prof -> ProfessionDto.fromEntity(prof))
                .collect(Collectors.toSet()),
            saved.getPrevitionalSystem(),
            saved.getHealthSystem(),
            saved.getPaymentMethod(),
            saved.getBank() != null ? saved.getBank().getId() : null,   // bankId
            saved.getBank() != null ? saved.getBank().getName() : null, // bankName
            saved.getBankAccountType(),
            saved.getBankAccountNumber(),
            saved.getContractType(),
            saved.getWorkSchedule(),
            saved.getShiftSystem(),
            saved.getShiftPattern(),
            saved.getPosition() != null ? PositionDto.fromEntity(saved.getPosition()) : null, // position
            saved.getPassword(),
            saved.getPhotoUrl(),
            saved.getHireDate(),
            saved.getBirthDate(),
            saved.getExitDate(),
            saved.getActive(),
            saved.getAddress(),
            UserDto.fromEntity(saved.getUser()),
            (saved.getProjects() == null ? Set.<Project>of() : saved.getProjects())
                .stream()
                .map(ProjectDto::fromEntity)
                .collect(Collectors.toSet())

        );

        return ResponseEntity.created(location).body(dto);
    }

}