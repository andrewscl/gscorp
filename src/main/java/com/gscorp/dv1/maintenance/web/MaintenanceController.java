package com.gscorp.dv1.maintenance.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/maintenance")
@AllArgsConstructor
public class MaintenanceController {

    @GetMapping("/dashboard")
    public String getMaintenanceDashboardView(Model model) {
        return "private/maintenance/views/maintenance-dashboard-view";
    }

    @GetMapping("/import-csv")
    public String getImportCsvView(Model model) {
        return "private/maintenance/views/import-csv-view";
    }

}
