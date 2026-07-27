package com.gscorp.dv1.operations.shiftassignments.web;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.shiftassignments.application.ShiftAssignmentService;
import com.gscorp.dv1.operations.shiftassignments.web.dto.ShiftAssignmentDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/shift-assignments")
@RequiredArgsConstructor
public class ShiftAssignmentController {

    private final ShiftAssignmentService shiftAssignmentService;

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

}
