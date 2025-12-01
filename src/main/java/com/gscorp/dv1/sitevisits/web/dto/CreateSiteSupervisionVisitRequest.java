package com.gscorp.dv1.sitevisits.web.dto;

import java.time.OffsetDateTime;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter @Setter
public class CreateSiteSupervisionVisitRequest {

    private Long siteId;
    private OffsetDateTime visitDateTime;
    private Double latitude;
    private Double longitude;
    private String description;
    private MultipartFile photo;
    private MultipartFile video;
    private String clientTimeZone;
    private String timezoneSource;

}
