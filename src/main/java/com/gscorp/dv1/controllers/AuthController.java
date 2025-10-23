package com.gscorp.dv1.controllers;


import java.io.IOException;
import java.net.URLEncoder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
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
    public String redirectToShell(@RequestParam String token,
                        HttpServletResponse response) throws IOException {
        String target = "/auth/define-password?token=" + URLEncoder.encode(token, "UTF-8");
        response.sendRedirect("/shell/auth?target=" + URLEncoder.encode(target, "UTF-8"));
        return null; // o void
    }

}
