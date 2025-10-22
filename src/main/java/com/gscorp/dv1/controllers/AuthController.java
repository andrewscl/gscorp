package com.gscorp.dv1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/signup")
    public String showSignUpForm () {
        return "auth/views/signup-view";
    }

    @GetMapping("/signin")
    public String showSignInForm () {
        return "auth/views/signin-view";
    }

    @GetMapping("/define-password")
    public String getDefinePasswordView(@RequestParam("token") String token, Model model) {
        //Redirige al Shell Auth y pasa el token como parametro
        return "redirect:/shell/auth?target=/auth/define-password?token=" + token;
    }

}
