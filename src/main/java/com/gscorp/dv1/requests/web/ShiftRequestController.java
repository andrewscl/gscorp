package com.gscorp.dv1.requests.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.requests.application.ShiftRequestService;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/shift-requests")
@AllArgsConstructor
public class ShiftRequestController {

    private final ShiftRequestService shiftRequestService;
    private final SiteService siteService;

    @GetMapping("/table-view")
    public String getShiftRequestsTableView (Model model) {
        model.addAttribute("shiftRequests",
                    shiftRequestService.findAll());
        return "private/requests/views/shift-requests-table-view";
    }

    @GetMapping("/create")
    public String getCreateShiftRequestView(Model model) {
        List<SiteDto> sites = siteService.getAllSites();
            model.addAttribute("sites", sites);
        return "private/requests/views/create-shift-request-view";
    }

    @GetMapping("/show/{id}")
    public String showShiftRequest (@PathVariable Long id, Model model){
        var shiftRequest = shiftRequestService.findById(id);
        model.addAttribute("shiftRequest", shiftRequest);
        return "private/shift-requests/views/view-shift-request-view";
    }

    @GetMapping("/edit/{id}")
    public String editShiftRequest (@PathVariable Long id, Model model){
        var shiftRequest = shiftRequestService.findById(id);
        model.addAttribute("shiftRequest", shiftRequest);
        return "private/shift-requests/views/edit-shift-request-view";
    }
}
