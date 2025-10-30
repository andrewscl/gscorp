package com.gscorp.dv1.supervisors.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/supervisors")
@RequiredArgsConstructor
public class SupervisorsController {

    @GetMapping("/dashboard")
    public String getSupervisorsDashboardView (Model model) {
        return "private/supervisors/views/supervisors-dashboard-view";
    }

}
