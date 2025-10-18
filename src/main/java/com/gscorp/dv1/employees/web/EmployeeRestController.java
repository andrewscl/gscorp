package com.gscorp.dv1.employees.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.employees.application.EmployeeCsvImportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeRestController {
    
    @Autowired
    private EmployeeCsvImportService employeeCsvImportService;

    @PostMapping("/import-csv")
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file) {
        try {
            employeeCsvImportService.importCsv(file);
            return ResponseEntity.ok("Importación exitosa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en la importación: " + e.getMessage());
        }
    }

}
