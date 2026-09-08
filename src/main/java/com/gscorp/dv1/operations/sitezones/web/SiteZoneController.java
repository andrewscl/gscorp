package com.gscorp.dv1.operations.sitezones.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.sites.application.SiteService;
import com.gscorp.dv1.operations.sites.web.dto.SiteDtoProjection;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/site-zones")
@RequiredArgsConstructor
public class SiteZoneController {

    private final SiteService siteService;
    private final UserScopeService userScopeService;

    @GetMapping("/{siteExternalId}/zones/new")
    public String createSiteZone(
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID siteExternalId
    ) {
        if(securityUser == null) return "redirect:/login";
        if(siteExternalId == null) return "redirect:/private/sites/table-view";
        ProjectScope scope = userScopeService.getProjectScope();
        SiteDtoProjection siteDto = siteService.findByExternalId(
            scope.ignoreFilter(), scope.projectIds(), siteExternalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found or access denied"));
        model.addAttribute("site", siteDto);
        return "private/operations/site-zones/create-site-zone";
    }
}
