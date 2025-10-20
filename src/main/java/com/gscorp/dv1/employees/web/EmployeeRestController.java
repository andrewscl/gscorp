package com.gscorp.dv1.employees.web;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.web.dto.CreateEmployeeRequest;
import com.gscorp.dv1.employees.web.dto.EmployeeDto;
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

        var entity = Employee.builder()
            .name(req.name().trim())
            .fatherSurname(req.fatherSurname())
            .motherSurname(req.motherSurname())
            .rut(req.rut())
            .mail(req.mail())
            .phone(req.phone())
            .secondaryPhone(req.secondaryPhone())
            .gender(req.gender())
            .nationality(req.nationality())
            .maritalStatus(req.maritalStatus())
            .studyLevel(req.studyLevel())
            .profession(req.profession())
            .previtionalSystem(req.previtionalSystem())
            .healthSystem(req.healthSystem())
            .paymentMethod(req.paymentMethod())
            .bankId(req.bankId())
            .bankName(req.bankName())
            .bankAccountType(req.bankAccountType())
            .bankAccountNumber(req.bankAccountNumber())
            .contractType(req.contractType())
            .workSchedule(req.workSchedule())
            .shiftSystem(req.shiftSystem())
            .position(req.position())
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
            saved.getNationality(),
            saved.getMaritalStatus(),
            saved.getStudyLevel(),
            saved.getProfession(),
            saved.getPrevitionalSystem(),
            saved.getHealthSystem(),
            saved.getPaymentMethod(),
            saved.getBankId(),
            saved.getBankName(),
            saved.getBankAccountType(),
            saved.getBankAccountNumber(),
            saved.getContractType(),
            saved.getWorkSchedule(),
            saved.getShiftSystem(),
            saved.getPosition(),
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