package com.gscorp.dv1.nationalities.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.nationalities.application.NationalityService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/nationalities")
@AllArgsConstructor
public class NationalityController {

    private final NationalityService nationalityService;

    @GetMapping("/table-view")
    public String getNationalitiesTableView(Model model) {
        model.addAttribute("nationalities", nationalityService.findAll());
        return "private/nationalities/views/nationalities-table-view";
    }

    @GetMapping("/create")
    public String createNationality(Model model) {
        return "private/nationalities/views/create-nationality-view";
    }

    @GetMapping("/show/{id}")
    public String showNationality(@PathVariable Long id, Model model){
        var nationality = nationalityService.findById(id);
        model.addAttribute("nationality", nationality);
        return "private/nationalities/views/view-nationality-view";
    }

    @GetMapping("/edit/{id}")
    public String editNationality(@PathVariable Long id, Model model){
        var nationality = nationalityService.findById(id);
        model.addAttribute("nationality", nationality);
        return "private/nationalities/views/edit-nationality-view";
    }

}
