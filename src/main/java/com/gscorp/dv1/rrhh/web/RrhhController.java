package com.gscorp.dv1.rrhh.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/rrhh")
@AllArgsConstructor
public class RrhhController {

    @GetMapping("/dashboard")
    public String getPrivateDashboardView(
            Model model) {

        return "private/rrhh/views/rrhh-dashboard-view";
    }

}
