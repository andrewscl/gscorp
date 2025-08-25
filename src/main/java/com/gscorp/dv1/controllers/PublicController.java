package com.gscorp.dv1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    @GetMapping("/home")
    public String showHomePage (Model model, HttpServletRequest req) {
        return render("public/views/index-view", model, req);
    }

    @GetMapping("/services")
    public String showServicesPage (Model model, HttpServletRequest req) {
        return render("public/views/services-view", model, req);
    }

    @GetMapping("/security")
    public String showSecurityPage (Model model, HttpServletRequest req) {
        return render("public/views/security-view", model, req);
    }

    @GetMapping("/clean")
    public String showCleanPage (Model model, HttpServletRequest req) {
        return render("public/views/clean-view", model, req);
    }

    @GetMapping("/outsourcing")
    public String showOutsourcingPage (Model model, HttpServletRequest req) {
        return render("public/views/outsourcing-view", model, req);
    }

    @GetMapping("/commerce")
    public String showCommercePage (Model model, HttpServletRequest req) {
        return render("public/views/commerce-view", model, req);
    }

    @GetMapping("/contact")
    public String showContactPage (Model model, HttpServletRequest req) {
        return render("public/views/contact-view", model, req);
    }

    @GetMapping("/us")
    public String showUsPage (Model model, HttpServletRequest req) {
        return render("public/views/us-view", model, req);
    }

    @GetMapping("/values")
    public String showValuesPage (Model model, HttpServletRequest req) {
        return render("public/views/values-view", model, req);
    }

    @GetMapping("/organization")
    public String showOrganizationPage (Model model, HttpServletRequest req) {
        return render("public/views/organization-view", model, req);
    }

    @GetMapping("/history")
    public String showHistoryPage (Model model, HttpServletRequest req) {
        return render("public/views/history-view", model, req);
    }

    @GetMapping("/governance")
    public String showGovernancePage (Model model, HttpServletRequest req) {
        return render("public/views/governance-view", model, req);
    }

    @GetMapping("/sustainability")
    public String showSustainabilityPage (Model model, HttpServletRequest req) {
        return render("public/views/sustainability-view", model, req);
    }

    @GetMapping("/presential-security")
    public String showPresentialSecurityPage (Model model, HttpServletRequest req) {
        return render("public/views/presential-security-view", model, req);
    }

    // ==== helper: decide layout + view (deep link) vs solo view (SPA) ====
    private String render(String view, Model model, HttpServletRequest req){
        // si el SPA hace fetch con este header, devuelve solo el view

        if("1".equals(req.getHeader("X-Fragment-Request"))
            || "1".equals(req.getParameter("fragment"))){
            return view;
        }

        // Deep Link / F5: devuelve el layout e inserta el view
        model.addAttribute("index", view);
        return "public/layouts/public-layout";
    }

}
