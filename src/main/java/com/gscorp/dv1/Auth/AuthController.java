package com.gscorp.dv1.auth;

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
    public String showSignUpForm () {
        return "auth/views/signup-view";
    }

    @GetMapping("/signin")
    public String showSignInForm () {
        return "auth/views/signin-view";
    }

    @GetMapping("/define-password")
    public String showDefinePasswordForm (@RequestParam("token") String token,
                        Model model, HttpServletRequest req) {

        // 1. Buscar el token
        Optional<PasswordResetToken> opt = passwordResetTokenService.findByToken(token);

        System.out.println("TOKEN RECIBIDO: " + token);
        System.out.println("TOKEN ENCONTRADO: " + opt);
        if (opt.isPresent()) {
            System.out.println("TOKEN EXPIRADO: " + opt.get().isExpired());
            System.out.println("TOKEN USADO: " + opt.get().isUsed());
        }

        // 2. Validar usando el service
        if (opt.isEmpty() || !passwordResetTokenService.isValid(opt.get())) {
            model.addAttribute("error", "El enlace es inválido o ha expirado.");
            return render("auth/views/invalid-token-view", model, req);
        }

        model.addAttribute("token", token);
        return render("auth/views/define-password-view", model, req);
    }

    // ==== helper: decide layout + view (deep link) vs solo view (SPA) ====
    private String render(String view, Model model, HttpServletRequest req){
        // si el SPA hace fetch con este header, devuelve solo el view

        if("1".equals(req.getHeader("X-Fragment-Request"))
            || "1".equals(req.getParameter("fragment"))){
            return view;
        }

        // Deep Link / F5: devuelve el layout e inserta el view
        model.addAttribute("auth", view);
        return "auth/layouts/auth-layout";
    }

}
