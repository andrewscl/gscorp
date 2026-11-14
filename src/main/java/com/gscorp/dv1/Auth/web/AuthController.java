package com.gscorp.dv1.auth.web;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.auth.application.PasswordResetTokenService;
import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PasswordResetTokenService passwordResetTokenService;

    @GetMapping("/signup")
    public String showSignUpForm (Model model, HttpServletRequest req) {
        return render("auth/views/signup-view", model, req);
    }

    @GetMapping("/signin")
    public String showSignInForm (Model model, HttpServletRequest req) {
        return render("auth/views/signin-view", model, req);
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm (Model model) {
        return "auth/fragments/forgot-password";
    }


    @GetMapping("/define-password")
    public String showDefinePasswordForm (
                    @RequestParam("token") String token,
                    Model model,
                    HttpServletRequest req) {

        Optional<PasswordResetToken> opt = passwordResetTokenService.findByToken(token);

        if (opt.isEmpty() || !passwordResetTokenService.isValid(opt.get()) || opt.get().getPurpose() == null) {
            model.addAttribute("error", "El enlace es inválido o ha expirado.");
            return render("auth/views/invalid-token-view", model, req);
        }

        model.addAttribute("token", token);

        return switch (opt.get().getPurpose()) {
                case INVITATION -> render("auth/views/define-password-view", model, req);
                case PASSWORD_RECOVERY -> render("auth/views/recover-password-view", model, req);
        };
    }


    private String render(String view, Model model, HttpServletRequest req){
        if("1".equals(req.getHeader("X-Fragment-Request"))
            || "1".equals(req.getParameter("fragment"))){
            return view;
        }
        model.addAttribute("auth", view);
        return "auth/layouts/auth-layout";
    }

}
