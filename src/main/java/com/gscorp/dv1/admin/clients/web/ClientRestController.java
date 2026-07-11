package com.gscorp.dv1.admin.clients.web;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.admin.clients.web.dto.ClientDto;
import com.gscorp.dv1.admin.clients.web.dto.CreateClientRequest;
import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.admin.projects.web.dto.ProjectSelectDto;
import com.gscorp.dv1.attendance.application.AttendanceServiceImpl;
import com.gscorp.dv1.operations.sitevisits.application.SiteVisitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientRestController {

    private final ClientService clientService;
    private final AttendanceServiceImpl attendanceService;
    private final SiteVisitService siteSupervisionVisitService;
    private final ProjectService projectService;


    @PostMapping("/create")
    public ResponseEntity <ClientDto> createClient(
        @Valid @RequestBody CreateClientRequest req,
        UriComponentsBuilder ucb) {

        ClientDto newClient = clientService.createClient(req);

        URI location = ucb
                        .path("/private/clients/{externalId}")
                        .buildAndExpand(newClient.externalId())
                        .toUri();

        return ResponseEntity.created(location).body(newClient);
    }


    @GetMapping("/all")
    public ResponseEntity<List<ClientDto>> getAllClients() {
        List<ClientDto> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id){
            clientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/kpis")
    public Map<String, Object> getKpis(@RequestParam Long clientId) {
        LocalDate today = LocalDate.now();

        long asistenciaHoy = attendanceService.countByClientIdAndDate(clientId, today);
        long visitasHoy = siteSupervisionVisitService.countByClientIdAndDate(clientId, today);

        return Map.of(
            "asistenciaHoy", asistenciaHoy,
            "visitasHoy", visitasHoy
        );
    }


    @GetMapping("/{clientId}/projects")
    public ResponseEntity<?> findProjectsByClient(@PathVariable("clientId") Long clientId) {
        try {
            log.debug("GET /api/clients/{}/projects", clientId);
            if (clientId == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "clientId requerido"));
            }
            List<ProjectSelectDto> projects = projectService.findByClientId(clientId);
            return ResponseEntity.ok(projects);
        } catch (Exception ex) {
            log.error("Error fetching projects for client {}: {}", clientId, ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(java.util.Map.of("message", "Error interno cargando proyectos", "detail", ex.getMessage()));
        }
    }

}
