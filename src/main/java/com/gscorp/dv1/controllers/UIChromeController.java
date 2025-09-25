package com.gscorp.dv1.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private")
public class UIChromeController {

    @GetMapping("/sidebar")
    public String sidebar(Authentication auth) {
        if (has(auth,"ROLE_ADMINISTRATOR"))
            return "private/admin/fragments/admin-menu-fragment :: adminSidebar";

        if (has(auth,"ROLE_OPS"))
            return "private/ops/fragments/ops-menu-fragment :: opsSidebar";

        if (has(auth,"ROLE_CLIENT"))
            return "private/client/fragments/client-menu-fragment :: clientSidebar";

        if (has(auth,"ROLE_RRHH"))        
            return "private/rrhh/fragments/rrhh-menu-fragment :: rrhhSidebar";

        return "private/fragments/sidebar-empty :: sidebar";
    }

    @GetMapping("/topbar")
    public String topbar(Authentication auth) {
        if (has(auth,"ROLE_ADMINISTRATOR"))
            return "private/admin/fragments/admin-menu-fragment :: adminTopbar";

        if (has(auth,"ROLE_OPS"))
            return "private/ops/fragments/ops-menu-fragment :: opsTopbar";

        if (has(auth,"ROLE_CLIENT"))
            return "private/client/fragments/client-menu-fragment :: clientTopbar";

        if (has(auth,"ROLE_RRHH"))
            return "private/rrhh/fragments/rrhh-menu-fragment :: rrhhTopbar";
            
        return "private/fragments/topbar-default :: topbar";
    }

    private boolean has(Authentication a, String role) {
        return a != null && a.getAuthorities().stream()
                 .anyMatch(ga -> ga.getAuthority().equals(role));
    }

}
