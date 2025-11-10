package com.gscorp.dv1.enums;

public enum Priority {
    
    LOW("Baja"),
    MEDIUM("Media"),
    HIGH("Alta"),
    CRITICAL("Crítica");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
