package com.gscorp.dv1.patrolexecution.web;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.security.SecurityUser;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/patrol-executions")
@RequiredArgsConstructor
public class PatrolExecutionController {


    @GetMapping("/free-execute/{patrolExternalId}")
    public String getFreePatrolExecution (
            Model model,
            Authentication authentication){

        if(authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if(!(principal instanceof SecurityUser)) {
            return "redirect:/login";
        }

        return "private/patrol-executions/views/free-patrol-execution-view";
    }

    @GetMapping("/schedule-execute/{patrolScheduleExternalId}")
    public String getSchedulePatrolExecution (
            @PathVariable UUID patrolExternalId,
            Model model,
            Authentication authentication){

        if(authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if(!(principal instanceof SecurityUser)) {
            return "redirect:/login";
        }

        model.addAttribute("patrolExternalId", patrolExternalId);
        return "private/patrol-executions/views/schedule-patrol-execution-view";
    }




}
