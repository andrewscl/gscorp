package com.gscorp.dv1.employees.web;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.attendance.application.AttendanceService;
import com.gscorp.dv1.attendance.web.dto.DashboardHeaderInfo;
import com.gscorp.dv1.bank.application.BankService;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.application.EmployeeTabsServiceImpl;
import com.gscorp.dv1.employees.web.dto.EmployeeTableDto;
import com.gscorp.dv1.employees.web.dto.EmployeeViewDto;
import com.gscorp.dv1.enums.BankAccountType;
import com.gscorp.dv1.enums.ContractType;
import com.gscorp.dv1.enums.Gender;
import com.gscorp.dv1.enums.HealthEntity;
import com.gscorp.dv1.enums.HealthSystem;
import com.gscorp.dv1.enums.MaritalStatus;
import com.gscorp.dv1.enums.PaymentMethod;
import com.gscorp.dv1.enums.PensionEntity;
import com.gscorp.dv1.enums.PrevitionalSystem;
import com.gscorp.dv1.enums.ShiftSystem;
import com.gscorp.dv1.enums.StudyLevel;
import com.gscorp.dv1.enums.WorkSchedule;
import com.gscorp.dv1.nationalities.application.NationalityService;
import com.gscorp.dv1.positions.application.PositionService;
import com.gscorp.dv1.positions.web.dto.PositionDto;
import com.gscorp.dv1.professions.application.ProfessionService;
import com.gscorp.dv1.professions.web.dto.ProfessionSelectDto;
import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.projects.web.dto.ProjectDto;
import com.gscorp.dv1.security.SecurityUser;
import com.gscorp.dv1.shiftpatterns.application.ShiftPatternService;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.web.dto.UserViewDto;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/employees")
@AllArgsConstructor
public class EmployeeController {

    private EmployeeService employeeService;
    private ProjectService projectService;
    private UserService userService;
    private NationalityService nationalityService;
    private ProfessionService professionService;
    private BankService bankService;
    private ShiftPatternService shiftPatternService;
    private PositionService positionService;
    private AttendanceService attendanceService;
    private EmployeeTabsServiceImpl employeeTabsService;
    
    @GetMapping("/dashboard")
    public String getPrivateDashboardView (
            Model model,
            Authentication authentication) {

            if(authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }

            Object principal = authentication.getPrincipal();
            if(!(principal instanceof SecurityUser)) {
                return "redirect:/login";
            }

            SecurityUser securityUser = (SecurityUser) principal;

            UUID externalId = securityUser.getUser().getExternalId();

            UserViewDto userViewDto = userService.findWithCompaniesAndClientsByExternalId(externalId);

            var employee = employeeService.findByIdViewEmployee(userViewDto.employeeId());

            LocalDateTime now = LocalDateTime.now();
            String formattedDate = now
                .format(DateTimeFormatter
                .ofPattern("EEEE, dd 'de' MMMM",
                                                new Locale("es", "ES")));

            DashboardHeaderInfo dashboardHeaderInfo = attendanceService.getDashboardHeader(externalId);

            model.addAttribute("employee", employee);
            model.addAttribute("currentDate", formattedDate);
            model.addAttribute("lastPunchText", dashboardHeaderInfo.lastPunchText());
            model.addAttribute("nextAction", dashboardHeaderInfo.nextAction());
            model.addAttribute("greeting", dashboardHeaderInfo.greeting());
            model.addAttribute("emoji", dashboardHeaderInfo.emoji());
            model.addAttribute("message", dashboardHeaderInfo.message());
        return "private/ops/views/ops-operator-dashboard-view";
    }

    @GetMapping("/table-view")
    public String getEmployeesTableView (
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int size
        ) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            // no autenticado: redirigir al login o devolver error según tu política
            return "redirect:/login";
        }

        if(authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if(!(principal instanceof SecurityUser)) {
                return "redirect:/login";
        }

        SecurityUser securityUser = (SecurityUser) principal;

        UUID externalId = securityUser.getUser().getExternalId();

        // Normalizar page/size (Spring Data usa 0-based)
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200); // límites: min 5, max 200

        // Normalizar q
        String safeQ = (q == null || q.trim().isEmpty()) ? null : q.trim();

        Page<EmployeeTableDto> employeesPage =
                        employeeService.getEmployeeTable(
                                externalId, safeQ, active, safePage, safeSize);

        List<PositionDto> positions = positionService.findAllProjection();

        List<ProjectDto> projects = projectService.findByUserExternalId(externalId);

        model.addAttribute("employeesPage", employeesPage);          // Page completo
        model.addAttribute("employees", employeesPage.getContent()); // Lista para iterar
        model.addAttribute("pageNumber", employeesPage.getNumber()); // 0-based
        model.addAttribute("pageSize", employeesPage.getSize());
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("totalElements", employeesPage.getTotalElements());
        model.addAttribute("positions", positions);
        model.addAttribute("projects", projects);

        // conservar filtros en la vista para los links
        model.addAttribute("q", safeQ);
        model.addAttribute("active", active);

        return "private/employees/views/employees-list";

    }

    @GetMapping("/create")
    public String getCreateEmployeeView (Model model) {

        model.addAttribute("employeeTabs", employeeTabsService.getCreateTabs());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("nationalities", nationalityService.findAll());
        model.addAttribute("maritalStatuses", MaritalStatus.values());
        model.addAttribute("studyLevels", StudyLevel.values());
        model.addAttribute("professions", professionService.findAll());
        model.addAttribute("previtionalSystems", PrevitionalSystem.values());
        model.addAttribute("pensionEntities", PensionEntity.values());
        model.addAttribute("healthSystems", HealthSystem.values());
        model.addAttribute("healthEntities", HealthEntity.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("banks", bankService.findAll());
        model.addAttribute("bankAccountTypes", BankAccountType.values());
        model.addAttribute("contractTypes", ContractType.values());
        model.addAttribute("workSchedules", WorkSchedule.values());
        model.addAttribute("shiftSystems", ShiftSystem.values());
        model.addAttribute("shiftPatterns", shiftPatternService.findAll());
        model.addAttribute("positions", positionService.findAll());
        model.addAttribute("projects", projectService.findAll());
        model.addAttribute("users", userService.findAll());

        return "private/employees/fragments/create-employee";
    }

    @GetMapping("/view/{externalId}")
    public String viewEmployee(
            @PathVariable UUID externalId,
            Model model,
            Authentication authentication
        ){

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return "redirect:/login";
        }

        EmployeeViewDto employee =
                employeeService.findByExternalIdViewEmployee(externalId);
        if (employee == null) {
            return "redirect:/private/employees";
        }

        model.addAttribute("employee", employee);
        model.addAttribute("employeeTabs", employeeTabsService.getTabs());
        model.addAttribute("employeeProfessions",
                    professionService.findProfessionSelectDtosByEmployeeId(externalId));
        model.addAttribute("employeeProjects",
                    projectService.findProjectSelectDtosByEmployeeExternalId(externalId));
        return "private/employees/fragments/view-employee";
    }


    @GetMapping("/edit/{externalId}")
    public String editEmployee(
                    @PathVariable UUID externalId,
                    Model model,
                    Authentication authentication
                    ){

        List<ProjectDto> projects = projectService.findByUserExternalId(externalId);
        List<ProfessionSelectDto> professions = professionService.findProfessionSelectDtosByEmployeeId(externalId);

        var employee = employeeService.findByExternalIdEditEmployee(externalId);
        

        List<Long> projectIds = projects
                                    .stream()
                                    .map(ProjectDto::id)
                                    .toList();

        List<Long> professionIds = professions
                                    .stream()
                                    .map(ProfessionSelectDto::id)
                                    .toList();

        model.addAttribute("employeeProfessions",
                    professionService.findProfessionSelectDtosByEmployeeId(externalId));
        model.addAttribute("employeeProjects",
                    projectService.findProjectSelectDtosByEmployeeExternalId(externalId));

        model.addAttribute("employee", employee);
        model.addAttribute("employeeTabs", employeeTabsService.getEditTabs());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("nationalities", nationalityService.findAll());
        model.addAttribute("maritalStatuses", MaritalStatus.values());
        model.addAttribute("studyLevels", StudyLevel.values());
        model.addAttribute("professions", professionService.findAll());
        model.addAttribute("previtionalSystems", PrevitionalSystem.values());
        model.addAttribute("pensionEntities", PensionEntity.values());
        model.addAttribute("healthSystems", HealthSystem.values());
        model.addAttribute("healthEntities", HealthEntity.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("banks", bankService.findAll());
        model.addAttribute("bankAccountTypes", BankAccountType.values());
        model.addAttribute("contractTypes", ContractType.values());
        model.addAttribute("workSchedules", WorkSchedule.values());
        model.addAttribute("shiftSystems", ShiftSystem.values());
        model.addAttribute("shiftPatterns", shiftPatternService.findAll());
        model.addAttribute("positions", positionService.findAll());
        model.addAttribute("projects", projectService.findAll());
        model.addAttribute("projectIds", projectIds);
        model.addAttribute("professionIds", professionIds);
        return "private/employees/fragments/edit-employee";
    }



}
