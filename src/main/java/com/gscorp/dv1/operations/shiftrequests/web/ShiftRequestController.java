package com.gscorp.dv1.operations.shiftrequests.web;

import java.time.LocalDate;
import java.time.ZoneId;
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

import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.operations.shiftpatterns.application.ShiftPatternService;
import com.gscorp.dv1.operations.shiftrequests.application.ShiftRequestService;
import com.gscorp.dv1.operations.shiftrequests.web.dto.ShiftRequestDtoWithSchedules;
import com.gscorp.dv1.operations.shiftrequests.web.dto.ShiftRequestSelectDto;
import com.gscorp.dv1.operations.shifts.application.ShiftService;
import com.gscorp.dv1.operations.shifts.web.dto.ShiftDto;
import com.gscorp.dv1.operations.sites.application.SiteService;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

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
    private final ShiftService shiftService;
    private final ShiftPatternService shiftPatternService;
    private final UserScopeService userScopeService;
    private final ProjectService projectService;

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

        Page<ShiftRequestSelectDto> shiftRequests =
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
    public String getCreateShiftRequestView(
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser) {
        if(securityUser == null) return "redirect:/login";
        ProjectScope scope = userScopeService.getProjectScope();
        model.addAttribute("projects", projectService.findByProjectIds(
                                scope.ignoreFilter(), scope.projectIds(), null));
        model.addAttribute("requestTypes", ShiftRequestType.values());
        return "private/operations/shift-requests/fragments/create-shift-request";
    }


    @GetMapping("/show/{externalId}")
    public String showShiftRequest (
                        @PathVariable UUID externalId,
                        Model model,
                        @AuthenticationPrincipal SecurityUser securityUser){
        populateShiftRequestModel(externalId, securityUser, model);
        return "private/operations/shift-requests/fragments/view-shift-request";
    }

    @GetMapping("/edit/{externalId}")
    public String editShiftRequest (
                        @PathVariable UUID externalId,
                        Model model,
                        @AuthenticationPrincipal SecurityUser securityUser){
        populateShiftRequestModel(externalId, securityUser, model);
        return "private/operations/shift-requests/fragments/edit-shift-request";
    }

    private void populateShiftRequestModel (
            UUID externalId, SecurityUser securityUser, Model model) {
        ProjectScope scope = userScopeService.getProjectScope();
        UUID userExternalId = securityUser.getUser().getExternalId();
        ShiftRequestDtoWithSchedules shiftRequestDto = shiftRequestService
                .findByExternalId(scope.ignoreFilter(), scope.projectIds(), externalId);
        Page<ShiftDto> shifts = shiftService.getLastShiftsByShiftRequest(
                                    userExternalId, externalId, 4, null);
        model.addAttribute("shiftRequest", shiftRequestDto);
        model.addAttribute("shiftsPage", shifts);
        model.addAttribute("shifts", shifts.getContent());
        model.addAttribute("shiftRequestStatuses", ShiftRequestStatus.values());
        model.addAttribute("shiftPatterns", shiftPatternService.getShiftPatternsList());
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

        Page<ShiftRequestSelectDto> shiftRequests =
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
