package com.gscorp.dv1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    @GetMapping("/home")
    public String showHomePage (Model model) {
        return "public/views/index-view";
    }

    @GetMapping("/security")
    public String showSecurityPage () {
        return "public/views/security-view";
    }

    @GetMapping("/clean")
    public String showCleanPage () {
        return "public/views/clean-view";
    }

    @GetMapping("/outsourcing")
    public String showOutsourcingPage () {
        return "public/views/outsourcing-view";
    }

    @GetMapping("/commerce")
    public String showCommercePage () {
        return "public/views/commerce-view";
    }

    @GetMapping("/contact")
    public String showContactPage () {
        return "public/views/contact-view";
    }

    @GetMapping("/values")
    public String showValuesPage () {
        return "public/views/values-view";
    }

    @GetMapping("/organization")
    public String showOrganizationPage () {
        return "public/views/organization-view";
    }

    @GetMapping("/history")
    public String showHistoryPage () {
        return "public/views/history-view";
    }

    @GetMapping("/governance")
    public String showGovernancePage () {
        return "public/views/governance-view";
    }

    @GetMapping("/sustainability")
    public String showSustainabilityPage () {
        return "public/views/sustainability-view";
    }

}
