package com.gscorp.dv1.licitations.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.licitations.application.LicitationSyncService;
import com.gscorp.dv1.licitations.infrastructure.LicitationRepository;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/licitations")
@AllArgsConstructor
public class LicitationController {
    
    @GetMapping("/dashboard")
    public String getLicitationsDashboardView(Model model) {
        return "private/licitations/views/licitations-dashboard-view";
    }

    @GetMapping("/mp-licitations")
    public String getLicitationsTableView(Model model) {
        model.addAttribute("licitations", licitationRepository.findAll());
        return "private/admin/views/licitations-table-view";
    }

    @GetMapping("/mp-licitations/sync")
    public String syncLicitations(Model model) {
        sync.syncTodayLicitations();
        return "private/admin/views/licitations-table-view";
    }

        @Autowired
    private LicitationRepository licitationRepository;

    @Autowired
    private final LicitationSyncService sync;

}
