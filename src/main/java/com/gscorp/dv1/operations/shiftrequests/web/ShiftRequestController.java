package com.gscorp.dv1.operations.shiftrequests.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.operations.shiftrequests.application.ShiftRequestService;
import com.gscorp.dv1.operations.shiftrequests.web.dto.ShiftRequestDtoWithSchedules;
import com.gscorp.dv1.operations.shiftrequests.web.dto.ShiftRequestDto;
import com.gscorp.dv1.operations.sites.application.SiteService;
import com.gscorp.dv1.operations.sites.web.dto.SiteDto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/private/shift-requests")
@AllArgsConstructor
public class ShiftRequestController {

    private final ShiftRequestService shiftRequestService;
    private final SiteService siteService;
    private final ZoneResolver zoneResolver;

    @GetMapping("/table-view")
    public String getShiftRequestsTableView (
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) String clientTz,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
        ){ 

        if(securityUser == null) return "redirect:/login";
        UUID externalId = securityUser.getUser().getExternalId();

        String cleanClientTz =
            (clientTz == null || clientTz.isBlank()) ? null : clientTz.trim();
        ZoneResolutionResult zoneResult =
                        zoneResolver.resolveZone(externalId, cleanClientTz);
        ZoneId zoneId = zoneResult.zoneId();

        Page<ShiftRequestDto> shiftRequests =
                shiftRequestService.getShiftRequestsTable(
                            externalId, zoneId,
                            null, null, null, null,
                            ShiftRequestType.FIXED, page, size);

        model.addAttribute("shiftRequestsPage", shiftRequests);
        model.addAttribute("shiftRequests", shiftRequests.getContent());
        model.addAttribute("count", shiftRequests.getTotalElements());
        model.addAttribute("sites", siteService.getAllSitesByUser(externalId));
        model.addAttribute("shiftRequestTypes", ShiftRequestType.values());

        return "private/operations/shift-requests/views/shift-request-list";
    }


    @GetMapping("/create")
    public String getCreateShiftRequestView(Model model) {
        List<SiteDto> sites = siteService.getAllSites();
            model.addAttribute("sites", sites);
            model.addAttribute("requestTypes", ShiftRequestType.values());
        return "private/operations/shift-requests/views/create-shift-request-view";
    }

    @GetMapping("/show/{id}")
    public String showShiftRequest (
                @PathVariable Long id,
                Model model,
                @AuthenticationPrincipal SecurityUser securityUser){

        if(securityUser == null) return "redirect:/login";
        UUID externalId = securityUser.getUser().getExternalId();

        try {
            ShiftRequestDtoWithSchedules shiftRequestDto =
                            shiftRequestService.getDtoIfOwned(id, externalId);
            model.addAttribute("shiftRequest", shiftRequestDto);
            return "private/operations/shift-requests/fragments/view-shift-request";
        } catch (Exception e) {
            return "redirect:/private/shift-requests/table-view";
        }
    }

    @GetMapping("/edit/{id}")
    public String editShiftRequest (
                        @PathVariable Long id,
                        Model model,
                        @AuthenticationPrincipal SecurityUser securityUser){

        if(securityUser == null) return "redirect:/login";
        UUID externalId = securityUser.getUser().getExternalId();
                            
        try {
            ShiftRequestDtoWithSchedules shiftRequestDto =
                                shiftRequestService.getDtoIfOwned(id, externalId);
            model.addAttribute("shiftRequest", shiftRequestDto);
            model.addAttribute("shiftRequestStatuses", ShiftRequestStatus.values());
            return "private/operations/shift-requests/fragments/edit-shift-request";
        } catch (Exception e) {
            return "redirect:/private/shift-requests/table-view";
        }
    }

    @GetMapping("/table-search")
    public String getShiftRequestTableSearch(
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(required = false) String clientTz,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) ShiftRequestType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {

        if(securityUser == null) return "redirect:/login";
        UUID externalId = securityUser.getUser().getExternalId();

        String cleanClientTz =
            (clientTz == null || clientTz.isBlank()) ? null : clientTz.trim();
        ZoneResolutionResult zoneResult =
                        zoneResolver.resolveZone(externalId, cleanClientTz);
        ZoneId zoneId = zoneResult.zoneId();

        if (from != null && to != null && from.isAfter(to)) {
            log.debug("from > to en request; intercambiando valores: from={}, to={}", from, to);
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        Page<ShiftRequestDto> shiftRequests =
                shiftRequestService.getShiftRequestsTable(
                            externalId, zoneId,
                            from, to, siteId, projectId,
                            type, page, size);

        model.addAttribute("shiftRequestsPage", shiftRequests);
        model.addAttribute("shiftRequests", shiftRequests.getContent());
        model.addAttribute("count", shiftRequests.getTotalElements());
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate",   to);
        return "private/operations/shift-requests/fragments/shift-request-table-rows :: rows";
    }


}
