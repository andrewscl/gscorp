package com.gscorp.dv1.sitesupervisionvisits.web.dto;

public record SiteVisitHourlyDto (
    Long siteId,
    String siteName,
    String hour,
    long count
){
    
}
