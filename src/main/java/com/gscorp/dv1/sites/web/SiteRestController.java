package com.gscorp.dv1.sites.web;

import java.util.TimeZone;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientRepo;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.web.dto.CreateSiteRequest;
import com.gscorp.dv1.sites.web.dto.SiteDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteRestController {

    private final SiteService siteService;
    private final ClientRepo clientRepo;

    @PostMapping("/create")
    public ResponseEntity <SiteDto> createSite(
        @Valid @RequestBody CreateSiteRequest req,
        UriComponentsBuilder ucb){
        
        //Resolver cliente
        Long clientId = req.clientId();
        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado" + clientId));

        //Normalizar campos opcionales
        String tz = (req.timeZone() == null || req.timeZone().isBlank())
                ? TimeZone.getDefault().getID()
                : req.timeZone().trim();

        Double lat = req.lat();  //Puede ser null
        Double lon = req.lon();    //Puede ser null

        //Construir entidad
        var entity = Site.builder()
            .client(client)
            .name(req.name().trim())
            .code(req.code())
            .address(req.address())
            .lat(lat)
            .lon(lon)
            .timeZone(tz)
            .active(req.active() == null ? true : req.active())
            .build();
        
        //Guardar
        var saved = siteService.saveSite(entity);

        //Respuesta
        var location = ucb.path("/api/sites/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = new SiteDto(
                saved.getId(),
                saved.getClient().getId(),
                saved.getName(),
                saved.getCode(),
                saved.getAddress(),
                saved.getClient().getName());
        return ResponseEntity.created(location).body(dto);

        }

    
}
