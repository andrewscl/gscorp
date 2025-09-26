package com.gscorp.dv1.users.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/admin/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @GetMapping("/show/{id}")
    public String showUser(@PathVariable Long id, Model model){
        var user = userService.findById(id);
        model.addAttribute("user", user);
        return "private/admin/users/views/view-user-view";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, Model model){
        var user = userService.findById(id);
        model.addAttribute("user", user);
        return "private/admin/users/views/edit-user-view";
    }
}
