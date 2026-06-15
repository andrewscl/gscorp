package com.gscorp.dv1.forecast.web;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.enums.Periodicity;
import com.gscorp.dv1.enums.Units;
import com.gscorp.dv1.forecast.application.ForecastService;
import com.gscorp.dv1.forecast.web.dto.ForecastFormPayload;
import com.gscorp.dv1.forecast.web.dto.ForecastTableRowDto;
import com.gscorp.dv1.security.SecurityUser;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteSelectDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/private/forecast")
@AllArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;
    private final UserService userService;
    private final ZoneResolver zoneResolver;
    private final SiteService siteService;

    // Método para acceder al vista de tabla de Forecasts
    @GetMapping("/table-view")
    public String getForecastTableView(
        Model model,
        Authentication authentication,
        @RequestParam(required = false) String siteName,
        @RequestParam(required = false) String metric,
        @RequestParam(required = false) String zone
    ) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            // no autenticado: redirigir al login o devolver error según tu política
            return "redirect:/login";
        }

        if(authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if(!(principal instanceof SecurityUser)) {
            return "redirect:/login";
        }

        SecurityUser securityUser = (SecurityUser) principal;

        UUID externalId = securityUser.getUser().getExternalId();

        // Resolver zona
        ZoneResolutionResult zr = zoneResolver.resolveZone(externalId, zone);
        ZoneId zoneId = zr.zoneId();

        List<SiteSelectDto> siteNames = siteService.findByUserExternalId(externalId);
        List<ForecastMetric> metrics = List.of(ForecastMetric.values());

        // Llamada al service pasando ZoneId validado
        List<ForecastTableRowDto> rows = forecastService
                                    .findRowsFilteredForUser(externalId, null, null, zoneId);

        // Poner datos en el model para la vista
        model.addAttribute("forecastRecords", rows);
        model.addAttribute("sites", siteNames);
        model.addAttribute("metrics", metrics);
        model.addAttribute("zone", zoneId.getId());       // p.ej. "Europe/Madrid" o "UTC"
        model.addAttribute("zoneSource", zr.source());

        return "private/forecast/views/forecast-table-view";
    }



    // Método para mostrar el formulario de creación de Forecast
    @GetMapping("/create")
    public String getForecastCreateForm(Model model, Authentication authentication) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            // según tu política puede ser redirect a login o acceso denegado
            return "redirect:/login";
        }

        if(authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if(!(principal instanceof SecurityUser)) {
            return "redirect:/login";
        }

        SecurityUser securityUser = (SecurityUser) principal;

        UUID externalId = securityUser.getUser().getExternalId();

        ForecastFormPayload formPayload;
        try {
            // Service recibe userId y parámetros opcionales (mejor para test)
            formPayload = forecastService.prepareCreateForecastForm(externalId);
        } catch (Exception ex) {
            log.error("Error preparando formulario createForecast para user {}: {}", userId, ex.getMessage(), ex);
            model.addAttribute("error", "No se pudo preparar el formulario.");
            model.addAttribute("prefill", null);
            model.addAttribute("clients", Collections.emptyList());
            return "private/forecast/views/create-forecast-view";
        }

    // Protección: formPayload puede ser null sin lanzar excepción; extraemos prefill de forma segura
    var prefill = formPayload != null ? formPayload.prefill() : null;

        String requested = formPayload != null && formPayload.prefill() != null ? formPayload.prefill().tz() : null;
        ZoneResolutionResult zr = zoneResolver.resolveZone(externalId, requested);

        // Añadir datos al modelo para que la vista los muestre
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
