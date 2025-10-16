package com.gscorp.dv1.analyst.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/analyst")
@AllArgsConstructor
public class AnalystController {

    @GetMapping("/dashboard")
    public String getAnalystDashboardView(Model model) {
        return "private/analyst/views/analyst-dashboard-view";
    }

}
