package com.gscorp.dv1.employees.web;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.web.dto.CreateEmployeeRequest;
import com.gscorp.dv1.employees.web.dto.EmployeeDto;
import com.gscorp.dv1.employees.web.dto.EmployeeViewDto;
import com.gscorp.dv1.employees.web.dto.UpdateEmployeeRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeRestController {

    private final EmployeeService employeeService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeDto> createEmployee(
        @Valid @ModelAttribute CreateEmployeeRequest req,
        UriComponentsBuilder ucb
    ) {

        Employee saved = employeeService.
                                createEmployeeFromRequest(req);
        var location = ucb.path("/api/employees/{externalId}")
                            .buildAndExpand(saved.getExternalId()).toUri();

        EmployeeDto dto = EmployeeDto.fromEntity(saved);

        return ResponseEntity.created(location).body(dto);

    }



    @PatchMapping(path = "/update/{externalId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> patchEmployee(
            @PathVariable("externalId") UUID externalId,
            @Valid @ModelAttribute UpdateEmployeeRequest updateEmployeeRequest) {

        // Validaciones iniciales
        if (externalId == null) {
            return ResponseEntity.badRequest().body(error("employeeId requerido"));
        }

        // Validación de al menos un campo enviado
        if (updateEmployeeRequest == null) {
            return ResponseEntity.badRequest().body(error("Se requiere al menos un campo o fotografía para actualizar."));
        }

        try {
            // Actualizar empleado en la base de datos
            Optional<EmployeeViewDto> updated = employeeService
                                        .updateEmployee(externalId, updateEmployeeRequest);

            if (updated.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("Empleado no encontrado"));
            }

            return ResponseEntity.ok(updated.get());

        } catch (IllegalArgumentException ex) {
            log.debug("Bad request updating employee {}: {}", externalId, ex.getMessage());
            return ResponseEntity.badRequest().body(error(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error actualizando empleado " + externalId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("Error interno actualizando empleado"));
        }
    }

    private static Map<String, String> error(String msg) {
        return Collections.singletonMap("message", msg);
    }



}