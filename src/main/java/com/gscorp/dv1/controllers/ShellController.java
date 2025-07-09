package com.gscorp.dv1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/shell")
@RequiredArgsConstructor
public class ShellController {
    
    @GetMapping("/auth")
    public String showAuthShell () {
        return "auth/layouts/auth-shell";
    }

    @GetMapping("/private")
    public String showPrivateShell () {
        return "private/layouts/private-shell";
    }

    @GetMapping("/public")
    public String showPublicShell () {
        return "public/layouts/public-shell";
    }

}
