package com.gscorp.dv1.employees.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.bank.application.BankService;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.infrastructure.EmployeeTableProjection;
import com.gscorp.dv1.enums.BankAccountType;
import com.gscorp.dv1.enums.ContractType;
import com.gscorp.dv1.enums.Gender;
import com.gscorp.dv1.enums.HealthSystem;
import com.gscorp.dv1.enums.HealthEntity;
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
import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.projects.web.dto.ProjectDto;
import com.gscorp.dv1.shiftpatterns.application.ShiftPatternService;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/employees")
@AllArgsConstructor
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private NationalityService nationalityService;

    @Autowired
    private ProfessionService professionService;
    
    @Autowired
    private BankService bankService;

    @Autowired
    private ShiftPatternService shiftPatternService;

    @Autowired
    private PositionService positionService;
    
    @GetMapping("/dashboard")
    public String getPrivateDashboardView (Model model) {
        return "private/employees/views/employee-dashboard-view";
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

        // Normalizar page/size (Spring Data usa 0-based)
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200); // límites: min 5, max 200

        // Normalizar q
        String safeQ = (q == null || q.trim().isEmpty()) ? null : q.trim();

        Page<EmployeeTableProjection> employeesPage =
                        employeeService.getEmployeeTable(
                                userId, safeQ, active, safePage, safeSize);

        List<PositionDto> positions = positionService.findAllProjection();

        List<ProjectDto> projects = projectService.findByUserId(userId);

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

        return "private/employees/views/employees-table-view";

    }


    @GetMapping("/create")
    public String getCreateEmployeeView (Model model) {

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

        return "private/employees/views/create-employee-view";
    }


    @GetMapping("/show/{id}")
    public String showEmployee(@PathVariable Long id, Model model){
        var employee = employeeService.findByIdViewEmployee(id);
        model.addAttribute("employee", employee);
        return "private/employees/views/view-employee-view";
    }


    @GetMapping("/edit/{id}")
    public String editEmployee(@PathVariable Long id, Model model){
        var employee = employeeService.findByIdEditEmployee(id);
        List<Long> projectIds = employeeService.findProjectIdsByEmployeeId(id);
        model.addAttribute("employee", employee);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("nationalities", nationalityService.findAll());
        model.addAttribute("maritalStatuses", MaritalStatus.values());
        model.addAttribute("studyLevels", StudyLevel.values());
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
        return "private/employees/views/edit-employee-view";
    }

}
