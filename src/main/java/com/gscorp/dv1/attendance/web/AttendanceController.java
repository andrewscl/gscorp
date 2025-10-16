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

import com.gscorp.dv1.attendance.infrastructure.AttendancePunch;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;
import com.gscorp.dv1.sites.application.SiteService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/attendance")
@AllArgsConstructor
public class AttendanceController {

    @Autowired
    private final AttendancePunchRepo attendanceRepo;

    @Autowired
    private final SiteService siteService;

    @GetMapping("/attdc-view")
    public String getAttendanceView (Model model){
        model.addAttribute("sites", siteService.getAllSites());
        return "private/attendance/views/attendance-view";
    }

    @GetMapping("/attdc-table")
    public String getAttendanceTableFragment(Model model) {
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
