package com.gscorp.dv1.forecast.web;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.forecast.application.ForecastService;
import com.gscorp.dv1.forecast.web.dto.ForecastFormPayload;
import com.gscorp.dv1.forecast.web.dto.ForecastRecordDto;
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
    private final ClientService clientService;

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

        // Resolver zona: preferir parámetro, luego zona del usuario (si userService la expone), luego system default
        ZoneId zoneId;
        try {
            if (zone != null && !zone.isBlank()) {
                zoneId = ZoneId.of(zone);
            } else {
                String userZone = null;
                try {
                    // userService.getUserZone is optional; si no existe, captura excepción y usa system default
                    //userZone = userService.getUserZone(userId);
                } catch (Exception ignored) { }
                zoneId = (userZone != null && !userZone.isBlank()) ? ZoneId.of(userZone) : ZoneId.systemDefault();
            }
        } catch (DateTimeException e) {
            model.addAttribute("error", "Zona horaria inválida: " + zone);
            // Mostrar vista vacía con error
            model.addAttribute("forecastRecords", List.of());
            model.addAttribute("clientIds", clientService.getClientIdsByUserId(userId));
            return "/private/forecast/views/forecast-table-view";
        }

        // Defaults para fechas: últimos 30 días
        LocalDate today = LocalDate.now(zoneId);
        if (to == null) to = today;
        if (from == null) from = to.minusDays(29);
        if (from.isAfter(to)) {
            model.addAttribute("error", "'from' no puede ser posterior a 'to'");
            model.addAttribute("forecastRecords", List.of());
            model.addAttribute("clientIds", clientService.getClientIdsByUserId(userId));
            return "/private/forecast/views/forecast-table-view";
        }

        // Obtener clientIds del usuario (para la vista de filtros o validaciones)
        List<Long> clientIds = clientService.getClientIdsByUserId(userId);
        model.addAttribute("clientIds", clientIds);

        // Llamada al servicio para obtener los registros (no paginado aquí; ver nota abajo)
        List<ForecastRecordDto> forecastRecords = forecastService.getForecastRecordsForUserByDates(
                userId,
                from,
                to,
                zoneId
                );

        model.addAttribute("forecastRecords", forecastRecords);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("zone", zoneId.getId());

        return "/private/forecast/views/forecast-table-view";
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
            return "/private/forecast/views/create-form";
        }

        // Añadir datos al modelo para que la vista los muestre
        model.addAttribute("prefill", formPayload.prefill());
        model.addAttribute("clients", formPayload.clients() == null ? Collections.emptyList() : formPayload.clients());
        model.addAttribute("zone", formPayload.prefill() != null ? formPayload.prefill().tz() : null);
        model.addAttribute("postUrl", "/private/forecast/create");
        model.addAttribute("cancelPath", "/private/forecast");

        return "/private/forecast/fragments/create-form";
    }
    
}
