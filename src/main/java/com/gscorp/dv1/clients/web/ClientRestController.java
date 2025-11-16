package com.gscorp.dv1.clients.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.attendance.application.AttendanceServiceImpl;
import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.web.dto.ClientDto;
import com.gscorp.dv1.clients.web.dto.ClientKpisDto;
import com.gscorp.dv1.clients.web.dto.CreateClientRequest;
import com.gscorp.dv1.incidents.application.IncidentService;
import com.gscorp.dv1.patrol.application.PatrolRunService;
import com.gscorp.dv1.sitesupervisionvisits.application.SiteSupervisionVisitService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientRestController {

    private final ClientService clientService;
    private final AttendanceServiceImpl attendanceService;
    private final SiteSupervisionVisitService siteSupervisionVisitService;
    private final PatrolRunService patrolRunService;
    private final IncidentService incidentService;


    @PostMapping("/create")
    public ResponseEntity <ClientDto> createClient(
        @jakarta.validation.Valid @RequestBody CreateClientRequest req,
        org.springframework.web.util.UriComponentsBuilder ucb) {
        var entity = Client.builder()
            .name(req.name().trim())
            .legalName(req.legalName())
            .taxId(req.taxId())
            .contactEmail(req.contactEmail())
            .contactPhone(req.contactPhone())
            .active(Boolean.TRUE.equals(req.active()))
            .build();
        var saved = clientService.saveClient(entity);  // que devuelva el guardado
        var location = ucb.path("/api/clients/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = new ClientDto(saved.getId(), saved.getName(), saved.getTaxId(),
            saved.getContactEmail(), saved.getActive());

        return ResponseEntity.created(location).body(dto);
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

    // Reemplazar el método antiguo por este en la clase ClientRestController
    @GetMapping("/dashboard/kpis")
    public ResponseEntity<ClientKpisDto> getClientDashboardKpis (
        @RequestParam(required = false, name = "clientIds") List<Long> clientIds,
        @RequestParam(required = false, name = "clientId") Long clientId, // compatibilidad legacy
        Authentication authentication
    ) {

    // resolver clientIds (usa ClientService para centralizar validación/roles)
    List<Long> resolved = clientService.resolveClientIdsOrDefault(authentication, clientIds, clientId);

    // si no hay clients, devolver 0s para evitar NPEs en frontend
    if (resolved == null || resolved.isEmpty()) {
        return ResponseEntity.ok(new ClientKpisDto(0L, 0L, 0L, 0L));
    }

    LocalDate today = LocalDate.now();

    String action = "IN";
    String tz = "America/Santiago"; // zona por defecto, da igual porque es para "hoy"

    long asistenciaHoy = attendanceService.countByClientIdsAndDate(resolved, today, action, tz);

    long rondasHoy = patrolRunService.countByClientIdsAndDate(resolved, today);

    long visitasHoy = siteSupervisionVisitService.countByClientIdsAndDate(resolved, today);

    long incidentesAbiertos = 0L;
    try {
        incidentesAbiertos = incidentService.countOpenByClientIds(resolved);
    } catch (Exception ignore) {
        // si no tienes incidentService implementado o falla, devolvemos 0 en vez de propagar error
    }

    ClientKpisDto dto = new ClientKpisDto(asistenciaHoy, rondasHoy, visitasHoy, incidentesAbiertos);
    return ResponseEntity.ok(dto);

    }

}
