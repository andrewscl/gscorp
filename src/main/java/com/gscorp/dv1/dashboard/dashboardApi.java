package com.gscorp.dv1.dashboard;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class dashboardApi {

    record PointDto (String x, double y){}

    @GetMapping("/series")
    public List<PointDto> series (
        @RequestParam String metric,
        @RequestParam Long empresaId,
        @RequestParam String from,
        @RequestParam String to,
        @RequestParam(defaultValue="day") String groupBy
    ) {
        return List.of(
            new PointDto("2025-09-01", 120),
            new PointDto("2025-09-02", 260),
            new PointDto("2025-09-03", 180),
            new PointDto("2025-09-04", 320)
        );
    }
    
}
