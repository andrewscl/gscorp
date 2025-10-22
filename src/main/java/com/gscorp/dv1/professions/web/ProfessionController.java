package com.gscorp.dv1.professions.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.professions.application.ProfessionService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/professions")
@AllArgsConstructor
public class ProfessionController {

    private final ProfessionService professionService;

    @GetMapping("/table-view")
    public String getProfessionsTableView(Model model) {
        model.addAttribute("professions", professionService.findAll());
        return "private/professions/views/professions-table-view";
    }

    @GetMapping("/create")
    public String getCreateProfessionView(Model model) {
        return "private/professions/views/create-profession-view";
    }

    @GetMapping("/show/{id}")
    public String showProfession(@PathVariable Long id, Model model){
        var profession = professionService.findById(id);
        model.addAttribute("profession", profession);
        return "private/professions/views/view-profession-view";
    }

    @GetMapping("/edit/{id}")
    public String editProfession(@PathVariable Long id, Model model){
        var profession = professionService.findById(id);
        model.addAttribute("profession", profession);
        return "private/professions/views/edit-profession-view";
    }

}
