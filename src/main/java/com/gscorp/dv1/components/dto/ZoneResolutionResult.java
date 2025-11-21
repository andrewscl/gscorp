package com.gscorp.dv1.components.dto;

import java.time.ZoneId;

public record ZoneResolutionResult (ZoneId zoneId, String source) {

    public static final String SOURCE_REQUESTED = "requested";
    public static final String SOURCE_USER = "user";
    public static final String SOURCE_SYSTEM = "system";
    
}
