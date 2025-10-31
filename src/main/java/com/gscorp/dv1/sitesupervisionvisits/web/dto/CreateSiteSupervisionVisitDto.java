package com.gscorp.dv1.sitesupervisionvisits.web.dto;

public record CreateSiteSupervisionVisitDto (
    Long siteId,
    Double latitude,
    Double longitude,
    String description,
    String photoPath,
    String videoPath
){
    
}
