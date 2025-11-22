package com.gscorp.dv1.forecast.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.enums.ForecastCategory;
import com.gscorp.dv1.enums.Periodicity;
import com.gscorp.dv1.forecast.application.ForecastService;
import com.gscorp.dv1.forecast.web.dto.ForecastFormPayload;
import com.gscorp.dv1.forecast.web.dto.ForecastTableRowDto;
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

    // Método para acceder al vista de tabla de Forecasts
    @GetMapping("/table-view")
    public String getForecastTableView(
        Model model,
        Authentication authentication,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required = false) String zone
    ) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            // no autenticado: redirigir al login o devolver error según tu política
            return "redirect:/login";
        }

        // Resolver zona
        ZoneResolutionResult zr = zoneResolver.resolveZone(userId, zone);
        ZoneId zoneId = zr.zoneId();

        // Normalizar fechas con la zona resuelta
        LocalDate today = LocalDate.now(zoneId);
        if (to == null) to = today;
        if (from == null) from = to.minusDays(29);
        if (from.isAfter(to)) {
            model.addAttribute("error", "'from' no puede ser posterior a 'to'");
            // aún devolvemos la vista con mensaje de error y sin filas
            model.addAttribute("forecastRecords", List.of());
            model.addAttribute("fromStr", from.format(DateTimeFormatter.ISO_LOCAL_DATE));
            model.addAttribute("toStr", to.format(DateTimeFormatter.ISO_LOCAL_DATE));
            model.addAttribute("zone", zoneId.getId());
            model.addAttribute("zoneSource", zr.source());
            return "private/forecast/views/forecast-table-view";
        }

        // Llamada al service pasando ZoneId validado
        List<ForecastTableRowDto> rows = forecastService.loadTableRowForUserAndDates(userId, from, to, zoneId);

        // Poner datos en el model para la vista
        model.addAttribute("forecastRecords", rows);
        model.addAttribute("fromStr", from.format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute("toStr", to.format(DateTimeFormatter.ISO_LOCAL_DATE));
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

        ForecastFormPayload formPayload;
        try {
            // Service recibe userId y parámetros opcionales (mejor para test)
            formPayload = forecastService.prepareCreateForecastForm(userId);
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
        ZoneResolutionResult zr = zoneResolver.resolveZone(userId, requested);

        // Añadir datos al modelo para que la vista los muestre
        model.addAttribute("prefill", prefill);
        model.addAttribute("periodicities", Periodicity.values());
        model.addAttribute("forecastCategories", ForecastCategory.values());
        model.addAttribute("clients", (formPayload != null && formPayload.clients() != null)
                                        ? formPayload.clients()
                                        : Collections.emptyList());
        model.addAttribute("zone", zr.zoneId().getId());
        model.addAttribute("postUrl", "/private/forecast/create");
        model.addAttribute("cancelPath", "/private/forecast");

        return "private/forecast/views/create-forecast-view";
    }
    
}
