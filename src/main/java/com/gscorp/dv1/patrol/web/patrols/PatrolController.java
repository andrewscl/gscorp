package com.gscorp.dv1.patrol.web.patrols;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.patrol.application.checkpoints.PatrolCheckpointService;
import com.gscorp.dv1.patrol.application.patrols.PatrolService;
import com.gscorp.dv1.patrol.web.checkpoints.dto.PatrolCheckpointDto;
import com.gscorp.dv1.patrol.web.patrols.dto.PatrolDto;
import com.gscorp.dv1.security.SecurityUser;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDtoProjection;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/patrols")
@RequiredArgsConstructor
public class PatrolController {

    private final UserService userService;
    private final PatrolService patrolService;
    private final SiteService siteService;
    private final PatrolCheckpointService PatrolCheckpointService;
    private final ObjectMapper objectMapper;

    private String googleCloudApiKey = System.getenv("GOOGLE_CLOUD_API_KEY");
    private String googleMapId = System.getenv("GOOGLE_MAP_ID");

    @GetMapping("/table-view")
    public String getPatrolsTableView(
                    Model model,
                    Authentication authentication) {

            if(authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }

            Object principal = authentication.getPrincipal();
            if(!(principal instanceof SecurityUser)) {
                return "redirect:/login";
            }

            SecurityUser securityUser = (SecurityUser) principal;

            UUID externalId = securityUser.getUser().getExternalId();

        List<PatrolDto> patrols = patrolService.getPatrolsByUserExtarnalUserId(externalId);
        if(patrols == null || patrols.isEmpty()) {
            model.addAttribute("infoMessage", 
            "No se encontraron rondas de supervisión asociadas a su usuario.");
        } else {
            model.addAttribute("patrols", patrols);
        }

        
        return "private/patrols/views/patrols-table-view";
    }

    @GetMapping("/create")
    public String getCreatePatrolForm (
                    Model model,
                    Authentication authentication) {

            if(authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }

            Object principal = authentication.getPrincipal();
            if(!(principal instanceof SecurityUser)) {
                return "redirect:/login";
            }

            SecurityUser securityUser = (SecurityUser) principal;

            UUID externalId = securityUser.getUser().getExternalId();

        List<SiteDtoProjection> sites = siteService
                            .findSiteProjectionsByUserExternalId(externalId);

        model.addAttribute("DayOfWeek", DayOfWeek.values());
        model.addAttribute("siteList", sites);
        return "private/patrols/views/create-patrol-view";
    }

    @GetMapping("/edit/{id}")
    public String getEditPatrolForm (
                    @PathVariable("id") String externalIdStr,
                    Model model,
                    Authentication authentication) {

        Long userId = userService
                            .getUserIdFromAuthentication(authentication);
        if(userId == null) {
            return "redirect:/login";
        }

        UUID externalId = UUID.fromString(externalIdStr);

        List<PatrolCheckpointDto> checkpoints =
            PatrolCheckpointService.getCheckpointsByExternalId(externalId);
        try {
            String checkpointsJson = objectMapper.writeValueAsString(checkpoints);
            model.addAttribute("checkpoints", checkpointsJson);
        } catch (JsonProcessingException e) {
            model.addAttribute("checkpoints", "[]");
        }

        //Buscar ragistro patrol por externalId
        PatrolDto patrol = patrolService
                            .getPatrolByExternalId(externalIdStr);

        model.addAttribute("patrol", patrol);
        model.addAttribute("DayOfWeek", DayOfWeek.values());
        System.out.println("Schedules encontrados: " + patrol.schedules().size());
        return "private/patrols/views/edit-patrol-view";
    }

    @GetMapping("/edit-map-picker/{externalId}/{siteId}")
    public String getMapPicker (
        @PathVariable Long siteId, //El sitio es parte de la ruta
        @PathVariable UUID externalId, //El sitio es parte de la ruta
        @RequestParam(defaultValue = "picker") String mode, // el modo es parametro
        Model model
    ){

        if (googleCloudApiKey == null || googleMapId == null) {
            throw new
            IllegalStateException("Faltan valores en Google Maps API Key o Map ID.");
        }

        List<PatrolCheckpointDto> checkpoints =
            PatrolCheckpointService.getCheckpointsByExternalId(externalId);
        try {
            String checkpointsJson = objectMapper.writeValueAsString(checkpoints);
            model.addAttribute("checkpoints", checkpointsJson);
        } catch (JsonProcessingException e) {
            model.addAttribute("checkpoints", "[]");
        }

        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("googlemapid", googleMapId);
        model.addAttribute("targetSiteId", siteId);
        model.addAttribute("patrolExternalId", externalId);
        model.addAttribute("mode", mode);

        return "private/patrols/views/patrols-map-picker-view";

    }

    @GetMapping("/view/{id}")
    public String getViewPatrolForm (
                    @PathVariable("id") String externalIdStr,
                    Model model,
                    Authentication authentication) {

        Long userId = userService
                            .getUserIdFromAuthentication(authentication);
        if(userId == null) {
            return "redirect:/login";
        }

        UUID externalId = UUID.fromString(externalIdStr);

        List<PatrolCheckpointDto> checkpoints =
            PatrolCheckpointService.getCheckpointsByExternalId(externalId);
        try {
            String checkpointsJson = objectMapper.writeValueAsString(checkpoints);
            model.addAttribute("checkpoints", checkpointsJson);
        } catch (JsonProcessingException e) {
            model.addAttribute("checkpoints", "[]");
        }

        //Buscar ragistro patrol por externalId
        PatrolDto patrol = patrolService
                            .getPatrolByExternalId(externalIdStr);

        model.addAttribute("patrol", patrol);
        model.addAttribute("DayOfWeek", DayOfWeek.values());
        System.out.println("Schedules encontrados: " + patrol.schedules().size());
        return "private/patrols/views/view-patrol-view";
    }


    @GetMapping("/patrol-dashboard")
    public String getPatrolsTableView (
                    Authentication authentication,
                    Model model
    ){

            if(authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            Object principal = authentication.getPrincipal();
            if(!(principal instanceof SecurityUser)) {
                return "redirect:/login";
            }

        return "private/patrols/views/patrol-dashboard-view";
    }    

}
