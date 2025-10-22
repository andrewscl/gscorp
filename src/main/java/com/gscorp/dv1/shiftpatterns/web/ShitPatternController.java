package com.gscorp.dv1.shiftpatterns.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.shiftpatterns.application.ShiftPatternService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/shift-patterns")
@AllArgsConstructor
public class ShitPatternController {

    private final ShiftPatternService shiftPatternService;

    @GetMapping("/table-view")
    public String getShiftPatternsTableView(Model model) {
        model.addAttribute("shiftPatterns", shiftPatternService.findAll());
        return "private/shift-patterns/views/shift-patterns-table-view";
    }

    @GetMapping("/create")
    public String createShiftPattern(Model model) {
        return "private/shift-patterns/views/create-shift-pattern-view";
    }

    @GetMapping("/show/{id}")
    public String showShiftPattern(@PathVariable Long id, Model model){
        var shiftPattern = shiftPatternService.findById(id);
        model.addAttribute("shiftPattern", shiftPattern);
        return "private/shift-patterns/views/view-shift-pattern-view";
    }

    @GetMapping("/edit/{id}")
    public String editShiftPattern(@PathVariable Long id, Model model){
        var shiftPattern = shiftPatternService.findById(id);
        model.addAttribute("shiftPattern", shiftPattern);
        return "private/shift-patterns/views/edit-shift-pattern-view";
    }
}
