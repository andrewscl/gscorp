package com.gscorp.dv1.operations.shiftrequests.web.dto.statistics;

public record HourlyComplianceMetrics(
    long totalDemand,
    long covered
) {

    public long pending() {return totalDemand - covered;}
    public double percentage() {
        return
            totalDemand > 0 ?
                Math.round((covered * 100.0 / totalDemand) * 100.0) / 100.0 : 0.0;
    }

}