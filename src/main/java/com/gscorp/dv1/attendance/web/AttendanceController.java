package com.gscorp.dv1.attendance.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.attendance.application.AttendanceService;
import com.gscorp.dv1.attendance.web.dto.AttendancePunchDto;
import com.gscorp.dv1.security.SecurityUser;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/private/attendance")
@AllArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final String googleCloudApiKey = System.getenv("GOOGLE_CLOUD_API_KEY");
    private final SiteService siteService;

    @GetMapping("/attdc-view")
    public String getAttendanceView (
            Model model,
        @AuthenticationPrincipal SecurityUser securityUser){

        if(securityUser == null) {
                return "redirect:/login";
        }
        UUID externalId = securityUser.getUser().getExternalId();

        List<SiteDto> sites = siteService.getAllSitesByUser(externalId);

        model.addAttribute("sites", sites);
        return "private/attendance/views/attendance-view";
    }


    @GetMapping("/table-view")
    public String getAttendanceTableFragment(
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required=false) String clientTz,
        @RequestParam(required=false) Long siteId,
        @RequestParam(required=false) Long projectId,
        @RequestParam(required=false) String action,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        if(securityUser == null) {
                return "redirect:/login";
        }
        UUID externalId = securityUser.getUser().getExternalId();

        if (from != null && to != null && from.isAfter(to)) {
            log.debug("from > to en request; intercambiando valores: from={}, to={}", from, to);
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        Page<AttendancePunchDto> punchsPage =
            attendanceService.getAttendanceTable(
                externalId, clientTz, from, to, siteId, projectId, action, page, size);

        model.addAttribute("punchsPage", punchsPage);
        model.addAttribute("punchs", punchsPage.getContent());
        model.addAttribute("punchsCount", punchsPage.getTotalElements());
        model.addAttribute("sites", siteService.getAllSitesByUser(externalId));
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);
        model.addAttribute("clientTimeZone", clientTz);
        return "private/attendance/views/attendance-list";
    }


    @GetMapping("/table-search")
    public String getAttendanceTableSearch(
        Model model,
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required=false) String clientTz,
        @RequestParam(required=false) String action,
        @RequestParam(required=false) Long siteId,
        @RequestParam(required=false) Long projectId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
        ) {

        if(securityUser == null) {
                return "redirect:/login";
        }
        UUID externalId = securityUser.getUser().getExternalId();

        if (from != null && to != null && from.isAfter(to)) {
            log.debug("from > to en request; intercambiando valores: from={}, to={}", from, to);
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        Page<AttendancePunchDto> punchsPage =
            attendanceService.getAttendanceTable(
                externalId, clientTz, from, to, siteId, projectId, action, page, size);

        model.addAttribute("punchsPage", punchsPage);
        model.addAttribute("punchs", punchsPage.getContent());
        model.addAttribute("punchsCount", punchsPage.getTotalElements());
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate",   to);
        return "private/attendance/fragments/attendance-table-rows :: rows";
    }


}
