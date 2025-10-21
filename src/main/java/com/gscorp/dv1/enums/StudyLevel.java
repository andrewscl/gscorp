package com.gscorp.dv1.enums;

public enum StudyLevel {
    Básica("Básica"),
    Media("Media"),
    Técnica("Técnica"),
    Universitaria("Universitaria"),
    Postgrado("Postgrado"),
    Doctorado("Doctorado");

    private final String displayName;

    StudyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
