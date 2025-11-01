package com.gscorp.dv1.enums;

public enum StudyLevel {
    BASICA("Básica"),
    MEDIA("Media"),
    TECNICA("Técnica"),
    UNIVERSITARIA("Universitaria"),
    POSTGRADO("Postgrado"),
    DOCTORADO("Doctorado");

    private final String displayName;

    StudyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
