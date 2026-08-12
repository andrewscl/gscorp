package com.gscorp.dv1.operations.shifts.web;

import java.time.LocalDate;
import java.util.UUID;

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
import com.gscorp.dv1.operations.sites.application.SiteService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;
    private final SiteService siteService;

    @GetMapping("/list")
    public String getShiftsTableView(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) UUID projectExternalId,
        @RequestParam(required = false) UUID siteExternalId,
        @RequestParam(required = false) ShiftStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size, // 💡 Corregido: Debe ser >= 1
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        if(securityUser == null) return "redirect:/login";
        UUID userExternalId = securityUser.getUser().getExternalId();

        LocalDate effectiveStartDate = (startDate != null) ? startDate : LocalDate.now();
        LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.now();

        Page<ShiftDto> shifts = shiftService.getShiftList(
                securityUser, effectiveStartDate, effectiveEndDate,
                                                    projectExternalId, siteExternalId, status, page, size);
        model.addAttribute("shiftsPage", shifts);
        model.addAttribute("shifts", shifts.getContent());
        model.addAttribute("count", shifts.getTotalElements());
        model.addAttribute("sites", siteService
                                                        .getAllSitesByUser(userExternalId));
        return "private/operations/shifts/views/shifts-list";
    }


    @GetMapping("/search")
    public String getShiftsListSearch(
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required=false) UUID projectId,
        @RequestParam(required=false) UUID siteId,
        @RequestParam(required=false) ShiftStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        if(securityUser == null) return "redirect:/login";

        Page<ShiftDto> shifts = shiftService.getShiftList(
                                    securityUser, 
                                    from, to,
                                    projectId, siteId,
                                    status, page, size);
        model.addAttribute("shiftsPage", shifts);
        model.addAttribute("shifts", shifts.getContent());
        model.addAttribute("count", shifts.getTotalElements());
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate",   to);
        return "private/operations/shifts/fragments/shifts-list-rows :: rows";
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
