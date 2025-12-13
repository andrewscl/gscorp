package com.gscorp.dv1.attendance.web;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.attendance.application.AttendanceService;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunch;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;
import com.gscorp.dv1.attendance.web.dto.AttendancePunchDto;
import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/private/attendance")
@AllArgsConstructor
public class AttendanceController {

    private final UserService userService;
    private final ZoneResolver zoneResolver;
    private final AttendanceService attendanceService;
    private final String googleCloudApiKey = System.getenv("GOOGLE_CLOUD_API_KEY");

    @Autowired
    private final AttendancePunchRepo attendanceRepo;


    @Autowired
    private final SiteService siteService;


    @GetMapping("/attdc-view")
    public String getAttendanceView (Model model){
        List<SiteDto> sites = siteService.getAllSites();
        model.addAttribute("sites", sites);
        return "private/attendance/views/attendance-view";
    }


    @GetMapping("/table-view")
    public String getAttendanceTableFragment(
        Model model,
        Authentication authentication,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required=false) String clientTz,
        @RequestParam(required=false) Long siteId,
        @RequestParam(required=false) Long projectId,
        @RequestParam(required=false) String action // "IN" | "OUT" |
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

        List<AttendancePunchDto> punchs = attendanceService
                                        .findByUserAndDateBetween(
                                            userId, from, to, resolvedZoneId, siteId, projectId, action);

        // cantidad de registros encontrados
        int punchsCount = punchs != null ? punchs.size() : 0;
        model.addAttribute("punchsCount", punchsCount);

        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("punchs", punchs);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);
        model.addAttribute("clientTimeZone", clientTz != null ? clientTz : zr.zoneId().getId());
        model.addAttribute("attendance", attendanceRepo.findAll());
        return "private/attendance/views/attendance-table-view";
    }


    @GetMapping("/attdc-filter")
    public String attendancePage(
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        Model model,
        Authentication auth) {

        // Rango por defecto: últimos 7 días
        LocalDate today = LocalDate.now();
        LocalDate start = (from != null) ? from : today.minusDays(6);
        LocalDate end   = (to   != null) ? to   : today;

        // Normaliza a OffsetDateTime (UTC o tu zona)
        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime fromTs = start.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime toTs   = end.plusDays(1).atStartOfDay(zone).toOffsetDateTime(); // fin exclusivo

        Long userId = currentUserId(auth); // ← tu forma de obtener el id del usuario

        // Para colaborador (solo sus marcaciones)
        List<AttendancePunch> items =
            attendanceRepo.findByUserIdAndTsBetweenOrderByTsDesc(userId, fromTs, toTs);

        // Si quisieras una vista global (admin) por fecha SIN usuario:
        // List<AttendancePunch> items =
        //     repo.findByTsBetweenOrderByTsDesc(fromTs, toTs);

        model.addAttribute("items", items);
        model.addAttribute("from", start);
        model.addAttribute("to",   end);
        return "private/attendance/views/attendance-table-view";
    }


    private Long currentUserId(Authentication auth){
        // TODO: extrae el id real desde tu UserDetails/JWT
        return 1L;
    }

}
