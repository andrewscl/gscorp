package com.gscorp.dv1.security.webauthn.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private")
public class webAuthnController {

    @GetMapping("/attendance")
        public String showSignUpForm () {
        return "private/webauthn/views/attendance-button-view";

    }

}
