package com.gscorp.dv1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private/default")
public class DefaultController {

    @GetMapping("/dashboard")
    public String getPrivateDefaultView(Model model) {
        return "private/default/views/default-dashboard-view";
    }
    
}
