package com.gscorp.dv1.sitesupervisionvisits.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sitesupervisionvisits.application.SiteVisitService;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/private/site-supervision-visits")
@RequiredArgsConstructor
public class SiteVisitController {

    private final SiteService siteService;
    private final String googleCloudApiKey = System.getenv("GOOGLE_CLOUD_API_KEY");
    private final SiteVisitService siteSupervisionVisitService;
    private final UserService userService;
    private final ZoneResolver zoneResolver;

    @GetMapping("/table-view")
    public String getSiteSupervisionVisitsTableView(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String clientTz
        ) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            // no autenticado: redirigir al login o devolver error según tu política
            return "redirect:/login";
        }

        // Resolve zone (requested clientTz takes precedence if valid; ZoneResolver handles fallbacks)
        ZoneResolutionResult zr = zoneResolver.resolveZone(userId, clientTz);
        ZoneId zone = zr.zoneId(); // usa zoneId() según tu record

        // Defaults: si no vienen parámetros, mostrar últimos 7 días (incluye hoy)
        LocalDate today = LocalDate.now(zone);
        if (to == null) {
            to = today;
        }
        if (from == null) {
            from = to.minusDays(7);
        }

        // Defensive: si from > to, intercambiar o devolver vacío; aquí intercambiamos por simplicidad
        if (from.isAfter(to)) {
            log.debug("from > to en request; intercambiando valores: from={}, to={}", from, to);
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        String resolvedZoneId = zone.getId();
        List<SiteVisitDto> visits = siteSupervisionVisitService
                                        .findByUserAndDateBetween(userId, from, to, resolvedZoneId);

        // cantidad de registros encontrados
        int visitsCount = visits != null ? visits.size() : 0;
        model.addAttribute("visitsCount", visitsCount);

        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("visits", visits);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);
        model.addAttribute("clientTimeZone", clientTz != null ? clientTz : zr.zoneId().getId());

        return "private/site-supervision-visits/views/site-supervision-visit-table-view";
    }

    @GetMapping("/create")
    public String createVisit(Model model) {
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        return "private/site-supervision-visits/views/create-site-sup-visit-view";
    }

    @GetMapping("/show/{id}")
    public String showSiteSupervisionVisit (@PathVariable Long id, Model model){
        var visit = siteSupervisionVisitService.findByIdWithEmployeeAndSite(id);
        model.addAttribute("visit", visit);
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        return "private/site-supervision-visits/views/view-site-supervision-visit-view";
    }

    @GetMapping("/edit/{id}")
    public String editSiteSupervisionVisit (@PathVariable Long id, Model model){
        var visit = siteSupervisionVisitService.findByIdWithEmployeeAndSite(id);
        model.addAttribute("visit", visit);
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        return "private/site-supervision-visits/views/edit-site-supervision-visit-view";
    }

    @GetMapping("/table-search")
    public String searchSiteSupervisionVisits(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String clientTz
    ) {
        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return "redirect:/login";
        }

        ZoneResolutionResult zr = zoneResolver.resolveZone(userId, clientTz);
        ZoneId zone = zr.zoneId();

        if (from == null && to == null) {
            return "redirect:/private/site-supervision-visits/table-view";
        }

        // Si sólo se aporta una fecha, la tratamos como rango de un día
        if (from == null && to != null) {
            from = to;
        } else if (to == null && from != null) {
            to = from;
        }

        // Si ambos no son null y from > to, intercambiamos
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        String resolvedZoneId = zone.getId();
        List<SiteVisitDto> visits = siteSupervisionVisitService
                                        .findByUserAndDateBetween(userId, from, to, resolvedZoneId);

        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("visits", visits);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);
        model.addAttribute("visitsCount", visits != null ? visits.size() : 0);
        model.addAttribute("clientTimeZone", clientTz != null ? clientTz : zr.zoneId().getId());

        return "private/site-supervision-visits/views/site-supervision-visit-table-view";
    }

}
