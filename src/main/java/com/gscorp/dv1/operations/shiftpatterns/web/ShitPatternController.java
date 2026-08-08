package com.gscorp.dv1.operations.shiftpatterns.web;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.shiftpatterns.application.ShiftPatternService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/shift-patterns")
@AllArgsConstructor
public class ShitPatternController {

    private final ShiftPatternService shiftPatternService;

    @GetMapping("/list")
    public String getShiftPatternsTableView(
                Model model,
                @AuthenticationPrincipal SecurityUser securityUser
    ) {
        if(securityUser == null) return "redirect:/login";
        model.addAttribute("shiftPatterns", shiftPatternService.getShiftPatternsList());
        return "private/configuration/shift-patterns/views/shift-patterns-list";
    }

    @GetMapping("/create")
    public String createShiftPattern(
                Model model,
                @AuthenticationPrincipal SecurityUser securityUser
    ) {
        if(securityUser == null) return "redirect:/login";
        return "private/configuration/shift-patterns/fragments/create-shift-pattern";
    }

    @GetMapping("/show/{shiftPatternExternalId}")
    public String showShiftPattern(
                @PathVariable UUID shiftPatternExternalId,
                Model model,
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if(securityUser == null) return "redirect:/login";
        
        model.addAttribute("shiftPattern",
                    shiftPatternService.findByExternalId(shiftPatternExternalId));
        return "private/configuration/shift-patterns/fragments/view-shift-pattern";
    }

    @GetMapping("/edit/{shiftPatternExternalId}")
    public String editShiftPattern(
                @PathVariable UUID shiftPatternExternalId,
                Model model,
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if(securityUser == null) return "redirect:/login";
        model.addAttribute("shiftPattern",
                    shiftPatternService.findByExternalId(shiftPatternExternalId));
        return "private/configuration/shift-patterns/fragments/edit-shift-pattern";
    }
}
