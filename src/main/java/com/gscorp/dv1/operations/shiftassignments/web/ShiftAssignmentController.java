package com.gscorp.dv1.operations.shiftassignments.web;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.operations.shiftassignments.application.ShiftAssignmentService;
import com.gscorp.dv1.operations.shiftassignments.web.dto.ShiftAssignmentDto;
import com.gscorp.dv1.operations.shiftpatterns.application.ShiftPatternService;
import com.gscorp.dv1.operations.shifts.application.ShiftService;
import com.gscorp.dv1.operations.shifts.web.dto.ShiftDto;
import com.gscorp.dv1.operations.sites.application.SiteService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/shift-assignments")
@RequiredArgsConstructor
public class ShiftAssignmentController {

    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftPatternService shiftPatternService;
    private final SiteService siteService;
    private final ProjectService projectService;
    private final ShiftService shiftService;

    @GetMapping("/list")
    public String getShiftAssignmentsList (
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int size
    ){
        if(securityUser == null) return "redirect:/login";
        UUID externalId = securityUser.getUser().getExternalId();

        Page<ShiftAssignmentDto> shiftAssignments =
                shiftAssignmentService.getShiftAssignmentList(externalId, null, page, size);

        model.addAttribute("shiftAssignmentsPage", shiftAssignments);
        model.addAttribute("shiftAssignments", shiftAssignments.getContent());
        model.addAttribute("count", shiftAssignments.getTotalElements());
        return "private/operations/shift-assignments/views/shift-assignments-list";
    }

    @GetMapping("/create")
    public String getCreateShiftAssignmentView(
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser
    ){
        if(securityUser == null) return "redirect:/login";
        UUID externalId = securityUser.getUser().getExternalId();

        model.addAttribute("sites",
                                siteService.findSiteProjectionsByUserExternalId(externalId));
        model.addAttribute("projects",
                                projectService.findByUserExternalId(externalId));
        model.addAttribute("shiftPatterns", shiftPatternService.getShiftPatternsList());
        return "private/operations/shift-assignments/fragments/create-shift-assignment";
    }


    @GetMapping("/view/{shiftAssignmentExternalId}")
    public String getViewShiftAssignment(
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable("shiftAssignmentExternalId") UUID shiftAssignmentExternalId
    ){
        if(securityUser == null) return "redirect:/login";
        UUID userExternalId = securityUser.getUser().getExternalId();
        List<ShiftDto> shifts = shiftService.getUpcomingByShiftAssignmentExternalId(
                                                    userExternalId,
                                                    shiftAssignmentExternalId,
                                                    5,
                                                    null);
        model.addAttribute("shiftAssignment",
            shiftAssignmentService.getByExternalId(shiftAssignmentExternalId));
        model.addAttribute("shifts" , shifts);
        return "private/operations/shift-assignments/fragments/view-shift-assignment";
    }

    @GetMapping("/edit/{shiftAssignmentExternalId}")
    public String getEditShiftAssignment(
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable("shiftAssignmentExternalId") UUID shiftAssignmentExternalId
    ){
        if(securityUser == null) return "redirect:/login";
        UUID userExternalId = securityUser.getUser().getExternalId();
        List<ShiftDto> shifts = shiftService.getUpcomingByShiftAssignmentExternalId(
                                                    userExternalId,
                                                    shiftAssignmentExternalId,
                                                    5,
                                                    null);
        model.addAttribute("shiftAssignment",
            shiftAssignmentService.getByExternalId(shiftAssignmentExternalId));
        model.addAttribute("shifts" , shifts);
        return "private/operations/shift-assignments/fragments/edit-shift-assignment";
    }

    @GetMapping("/close/{shiftAssignmentExternalId}")
    public String closeEditShiftAssignment(
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable("shiftAssignmentExternalId") UUID shiftAssignmentExternalId
    ){
        if(securityUser == null) return "redirect:/login";
        List<ShiftAssignmentStatus> allowedShiftStatuses = List.of(
            ShiftAssignmentStatus.CANCELLED, ShiftAssignmentStatus.FINISHED
        );
        model.addAttribute("shiftAssignment",
            shiftAssignmentService.getByExternalId(shiftAssignmentExternalId));
        model.addAttribute("allowedShiftStatuses", allowedShiftStatuses);
        return "private/operations/shift-assignments/fragments/close-shift-assignment";
    }

}
