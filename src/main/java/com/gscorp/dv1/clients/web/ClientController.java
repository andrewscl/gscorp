package com.gscorp.dv1.clients.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.clients.infrastructure.ClientRepo;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/clients")
@AllArgsConstructor
public class ClientController {

    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private ClientService clientService;

    @GetMapping("/table-view")
    public String getClientsTableView(Model model) {
        model.addAttribute("clients", clientRepo.findAll());
        return "private/clients/views/clients-table-view";
    }

    @GetMapping("/show/{id}")
    public String showClient(@PathVariable Long id, Model model){
        var client = clientService.findWithUsersById(id);
        model.addAttribute("client", client);
        return "private/clients/views/view-client-view";
    }

    @GetMapping("/edit/{id}")
    public String editClient(@PathVariable Long id, Model model){
        var client = clientService.findWithUsersById(id);
        model.addAttribute("client", client);
        return "private/clients/views/edit-client-view";
    }

}
