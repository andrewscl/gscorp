package com.gscorp.dv1.attendance.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/attendance")
@AllArgsConstructor
public class AttendanceController {

    @GetMapping("/attendance")
    public String getAttendanceView (Model model){
        return "private/attendance/views/attendance-view";
    }

}
