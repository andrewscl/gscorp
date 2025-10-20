package com.gscorp.dv1.requests.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/requests")
@RequiredArgsConstructor
public class ShiftRequestController {

    @GetMapping("/table-view")
    public String getShiftRequestsTableView(Model model) {
        return "private/requests/views/shift-request-table-view";
    }

}
