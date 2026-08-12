package com.gscorp.dv1.operations.shifts.web;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.operations.shifts.application.ShiftService;
import com.gscorp.dv1.operations.shifts.web.dto.ShiftDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping("/list")
    public String getShiftsTableView(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Long projectId,
        @RequestParam(required = false) Long siteId,
        @RequestParam(required = false) ShiftStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size, // 💡 Corregido: Debe ser >= 1
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        if(securityUser == null) return "redirect:/login";

        // 💡 Si el usuario no envía fechas, por defecto muestra los turnos de HOY
        LocalDate effectiveStartDate = (startDate != null) ? startDate : LocalDate.now();
        LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.now();

        Page<ShiftDto> shifts = shiftService.getShiftList(
                                    securityUser, 
                                    effectiveStartDate, effectiveEndDate,
                                    projectId, siteId,
                                    status, page, size);
        model.addAttribute("shiftsPage", shifts);
        model.addAttribute("shifts", shifts.getContent());
        model.addAttribute("count", shifts.getTotalElements());
        return "private/operations/shifts/views/shifts-list";
    }

    @GetMapping("/create")
    public String createShift(Model model) {

        return "private/shifts/views/create-shift-view";
    }

    @GetMapping("/show/{id}")
    public String showShift(@PathVariable Long id, Model model) {

        return "private/shifts/views/view-shift-view";
    }

    @GetMapping("/edit/{id}")
    public String editShift(@PathVariable Long id, Model model) {

        return "private/shifts/views/edit-shift-view";
    }

}
