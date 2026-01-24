package com.gscorp.dv1.shifts.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final SiteService siteService;
    private final UserService userService;

    @GetMapping("/table-view")
    public String getShiftsTableView(Model model,
    @AuthenticationPrincipal User authenticatedUser) {
        model.addAttribute("sites", 
                        siteService.getAllSites());
        model.addAttribute("isAdmin",
                        userService.isAdmin(authenticatedUser));
        return "private/shifts/views/shifts-table-view";
    }

    @GetMapping("/create")
    public String createShift(Model model) {
        model.addAttribute("sites", 
                        siteService.getAllSites());
        return "private/shifts/views/create-shift-view";
    }

    @GetMapping("/show/{id}")
    public String showShift(@PathVariable Long id, Model model) {
        model.addAttribute("sites", 
                        siteService.getAllSites());
        return "private/shifts/views/view-shift-view";
    }

    @GetMapping("/edit/{id}")
    public String editShift(@PathVariable Long id, Model model) {
        model.addAttribute("sites", 
                        siteService.getAllSites());
        return "private/shifts/views/edit-shift-view";
    }

}
