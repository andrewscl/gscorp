package com.gscorp.dv1.shiftrequests.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
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
import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.shiftrequests.application.ShiftRequestService;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDto;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDtoLight;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/private/shift-requests")
@AllArgsConstructor
public class ShiftRequestController {

    private final ShiftRequestService shiftRequestService;
    private final SiteService siteService;
    private final UserService userService;
    private final ZoneResolver zoneResolver;

    @GetMapping("/table-view")
    public String getShiftRequestsTableView (
            Model model,
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(required = false)
            String clientTz,
            @RequestParam(required = false)
            Long siteId,
            @RequestParam(required = false)
            ShiftRequestType type
        ){ 

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            // no autenticado: redirigir al login o devolver error según tu política
            return "redirect:/login";
        }

        // Resolve zone (requested clientTz takes precedence if valid; ZoneResolver handles fallbacks)
        ZoneResolutionResult zr = zoneResolver.resolveZone(userId, clientTz);
        ZoneId zone = zr.zoneId();

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
        List<ShiftRequestDtoLight> shiftRequests = shiftRequestService
                                        .findByUserIdAndDateBetween(
                                                userId,
                                                from,
                                                to,
                                                resolvedZoneId,
                                                siteId,
                                                type);

        model.addAttribute("shiftRequestsCount", shiftRequests.size());
        model.addAttribute("shiftRequests", shiftRequests);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);
        model.addAttribute("clientTimeZone"
                                            , clientTz != null ? clientTz : resolvedZoneId);

        return "private/shift-requests/views/shift-request-table-view";
    }

    @GetMapping("/create")
    public String getCreateShiftRequestView(Model model) {
        List<SiteDto> sites = siteService.getAllSites();
            model.addAttribute("sites", sites);
            model.addAttribute("requestTypes", ShiftRequestType.values());
        return "private/shift-requests/views/create-shift-request-view";
    }

    @GetMapping("/show/{id}")
    public String showShiftRequest (@PathVariable Long id, Model model, Authentication authentication){
        Long userId = userService.getUserIdFromAuthentication(authentication);
        try {
            ShiftRequestDto shiftRequestDto = shiftRequestService.getDtoIfOwned(id, userId);
            model.addAttribute("shiftRequest", shiftRequestDto);
            return "private/shift-requests/views/view-shift-request-view";
        } catch (Exception e) {
            return "redirect:/private/shift-requests/table-view";
        }
    }

    @GetMapping("/edit/{id}")
    public String editShiftRequest (
                        @PathVariable Long id,
                        Model model,
                        Authentication authentication){
        Long userId = userService.getUserIdFromAuthentication(authentication);
        try {
            ShiftRequestDto shiftRequestDto = shiftRequestService.getDtoIfOwned(id, userId);
            model.addAttribute("shiftRequest", shiftRequestDto);
            return "private/shift-requests/views/edit-shift-request-view";
        } catch (Exception e) {
            return "redirect:/private/shift-requests/table-view";
        }
    }

    @GetMapping("/table-search")
    public String getShiftRequestTableSearch(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(required = false)
            String clientTz,
            @RequestParam(required = false)
            Long siteId,
            @RequestParam(required = false)
            ShiftRequestType type
    ) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return "redirect:/login";
        }

        // normalización
        if (from == null && to == null) {
            model.addAttribute("shiftRequests", Collections.emptyList());
            model.addAttribute("shiftRequestsCount", 0);
            return "private/shift-requests/fragments/shift-request-table-rows :: rows";
        }

        if (from == null && to != null) from = to;
        if (to == null && from != null) to = from;
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from; from = to; to = tmp;
        }

        List <ShiftRequestDtoLight> shiftRequests = shiftRequestService
                                        .findByUserIdAndDateBetween(
                                                userId,
                                                from,
                                                to,
                                                clientTz,
                                                siteId,
                                                type);

        model.addAttribute("shiftRequests", shiftRequests);
        model.addAttribute("shiftRequestsCount", shiftRequests.size());

        return "private/shift-requests/fragments/shift-request-table-rows :: rows";

    }


}
