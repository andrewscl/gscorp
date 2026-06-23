package com.gscorp.dv1.operations.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/operations")
@AllArgsConstructor
public class OperationsController {

    @GetMapping("/dashboard/analyst")
    public String getAnalystDashboardView(Model model) {
        return "private/ops/dashboards/templates/analyst-dashboard-view";
    }

}
