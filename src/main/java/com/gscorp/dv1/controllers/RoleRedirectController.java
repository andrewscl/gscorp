package com.gscorp.dv1.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RoleRedirectController {

    @GetMapping("/private")
    public String redirectByRole(Authentication auth,
                @RequestParam(value="fragment", required=false)
                    String fragment) {

        if( auth==null || !auth.isAuthenticated()){
            return "redirect:/auth/signin";
        }

        boolean isFragment = (fragment != null);
        String prefix = isFragment ? "forward:/" : "redirect:/";
        String suffix = isFragment ? "?fragment=1": "";

        if(has(auth, "ROLE_ADMINISTRATOR"))
            return prefix + "admin/dashboard" + suffix;

        if(has(auth, "ROLE_OPS"))
            return prefix + "ops/dashboard" + suffix;

        if(has(auth, "ROLE_CLIENT"))
            return prefix + "client/dashboard" + suffix;

        return prefix + "private/dashboard" + suffix;

    }

    private boolean has(Authentication auth, String role) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a-> a.getAuthority().equals(role));
    }

}