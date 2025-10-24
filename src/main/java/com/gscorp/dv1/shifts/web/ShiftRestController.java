package com.gscorp.dv1.shifts.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.shifts.application.ShiftService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shift-patterns")
@RequiredArgsConstructor
public class ShiftRestController {

    private final ShiftService shiftService;

    @PoastMapping("/create")
    public ResponseEntity<ShiftDto> createShift(
    
}