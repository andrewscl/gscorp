package com.gscorp.dv1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

}
