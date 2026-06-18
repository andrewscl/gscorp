package com.gscorp.dv1.hr.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/rrhh")
@AllArgsConstructor
public class HrController {

    @GetMapping("/dashboard")
    public String getPrivateDashboardView(
            Model model) {

        return "private/hr/dashboards/templates/hr-dashboard-view";
    }

}
