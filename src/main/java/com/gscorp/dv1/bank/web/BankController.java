package com.gscorp.dv1.bank.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.bank.application.BankService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/banks")
@AllArgsConstructor
public class BankController {

    @Autowired
    private BankService bankService;

    @GetMapping("/table-view")
    public String getBanksTableView(Model model) {
        model.addAttribute("banks", bankService.findAll());
        return "private/banks/views/banks-table-view";
    }

    @GetMapping("/create")
    public String createBank(Model model) {
        return "private/banks/views/create-bank-view";
    }

    @GetMapping("/show/{id}")
    public String showBank(@PathVariable Long id, Model model){
        var bank = bankService.findById(id);
        model.addAttribute("bank", bank);
        return "private/banks/views/view-bank-view";
    }

    @GetMapping("/edit/{id}")
    public String editBank(@PathVariable Long id, Model model){
        var bank = bankService.findById(id);
        model.addAttribute("bank", bank);
        return "private/banks/views/edit-bank-view";
    }

}
