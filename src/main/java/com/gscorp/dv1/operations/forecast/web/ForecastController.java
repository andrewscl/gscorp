package com.gscorp.dv1.operations.forecast.web;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.enums.Periodicity;
import com.gscorp.dv1.enums.Units;
import com.gscorp.dv1.operations.forecast.application.ForecastService;
import com.gscorp.dv1.operations.forecast.web.dto.ForecastFormPayload;
import com.gscorp.dv1.operations.forecast.web.dto.ForecastTableRowDto;
import com.gscorp.dv1.operations.sites.application.SiteService;
import com.gscorp.dv1.operations.sites.web.dto.SiteDto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/private/forecast")
@AllArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;
    private final ZoneResolver zoneResolver;
    private final SiteService siteService;

    @GetMapping("/table-view")
    public String getForecastTableView(
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(required = false) String siteName,
        @RequestParam(required = false) String metric,
        @RequestParam(required = false) String zone
    ) {
        UUID userExternalId = securityUser.getUser().getExternalId();
        ZoneResolutionResult zr = zoneResolver.resolveZone(userExternalId, zone);
        ZoneId zoneId = zr.zoneId();
        List<SiteDto> siteNames = siteService.findByUserScope();
        List<ForecastMetric> metrics = List.of(ForecastMetric.values());
        // Llamada al service pasando ZoneId validado
        List<ForecastTableRowDto> rows = forecastService
                                    .findRowsFilteredForUser(
                                        userExternalId,
                                        null, 
                                        null,
                                        zoneId);
        model.addAttribute("forecastRecords", rows);
        model.addAttribute("sites", siteNames);
        model.addAttribute("metrics", metrics);
        model.addAttribute("zone", zoneId.getId());       // p.ej. "Europe/Madrid" o "UTC"
        model.addAttribute("zoneSource", zr.source());

        return "private/forecast/views/forecast-table-view";
    }


    @GetMapping("/create")
    public String getForecastCreateForm(
                    @AuthenticationPrincipal SecurityUser securityUser,
                    Model model) {

        ForecastFormPayload formPayload;
        try {
            // Service recibe userId y parámetros opcionales (mejor para test)
            formPayload = forecastService.prepareCreateForecastForm(securityUser);
        } catch (Exception ex) {
            log.error("Error preparando formulario createForecast para user {}: {}", securityUser.getUser(), ex.getMessage(), ex);
            model.addAttribute("error", "No se pudo preparar el formulario.");
            model.addAttribute("prefill", null);
            model.addAttribute("clients", Collections.emptyList());
            return "private/forecast/views/create-forecast-view";
        }

        var prefill = formPayload != null ? formPayload.prefill() : null;

        String requested = formPayload != null && formPayload.prefill() != null ? formPayload.prefill().tz() : null;
        ZoneResolutionResult zr = zoneResolver.resolveZone(securityUser.getUser().getExternalId(), requested);

        model.addAttribute("prefill", prefill);
        model.addAttribute("periodicities", Periodicity.values());
        model.addAttribute("forecastMetrics", ForecastMetric.values());
        model.addAttribute("unitsList", Units.values());
        model.addAttribute("clients", (formPayload != null && formPayload.clients() != null)
                                        ? formPayload.clients()
                                        : Collections.emptyList());
        model.addAttribute("zone", zr.zoneId().getId());
        model.addAttribute("postUrl", "/private/forecast/create");
        model.addAttribute("cancelPath", "/private/forecast");

        return "private/forecast/views/create-forecast-view";
    }

}
