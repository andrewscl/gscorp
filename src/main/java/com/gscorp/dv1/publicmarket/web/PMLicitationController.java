package com.gscorp.dv1.publicmarket.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.publicmarket.infrastructure.PMLicitationRepository;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/licitations")
@AllArgsConstructor
public class PMLicitationController {

    @GetMapping("/dashboard")
    public String getLicitationsDashboardView(Model model) {
        return "private/licitations/views/licitations-dashboard-view";
    }


    @GetMapping("/mp-licitations")
    public String getLicitationsTableView(Model model) {
        model.addAttribute("licitations", licitationRepository.findAll());
        return "private/admin/views/licitations-table-view";
    }

    @Autowired
    private PMLicitationRepository licitationRepository;

}
