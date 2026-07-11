package com.gscorp.dv1.operations.patrolexecution.web;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.config.security.SecurityUser;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/patrol-executions")
@RequiredArgsConstructor
public class PatrolExecutionController {


    @GetMapping("/schedule-execute/{patrolScheduleExternalId}")
    public String getSchedulePatrolExecution (
            @PathVariable UUID patrolScheduleExternalId,
            Model model,
            Authentication authentication){

        if(authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if(!(principal instanceof SecurityUser)) {
            return "redirect:/login";
        }

        model.addAttribute("patrolScheduleExternalId", patrolScheduleExternalId);
        return "private/patrol-executions/views/patrol-execution-view";
    }




}
