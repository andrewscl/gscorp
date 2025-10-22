package com.gscorp.dv1.positions.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.positions.application.PositionService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/positions")
@AllArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping("/table-view")
    public String getPositionsTableView(Model model) {
        model.addAttribute("positions", positionService.findAll());
        return "private/positions/views/positions-table-view";
    }

    @GetMapping("/create")
    public String createPosition(Model model) {
        return "private/positions/views/create-position-view";
    }

    @GetMapping("/show/{id}")
    public String showPosition(@PathVariable Long id, Model model){
        var position = positionService.findById(id);
        model.addAttribute("position", position);
        return "private/positions/views/view-position-view";
    }

    @GetMapping("/edit/{id}")
    public String editPosition(@PathVariable Long id, Model model){
        var position = positionService.findById(id);
        model.addAttribute("position", position);
        return "private/positions/views/edit-position-view";
    }
}
