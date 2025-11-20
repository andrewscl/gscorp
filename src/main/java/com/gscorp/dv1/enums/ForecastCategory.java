package com.gscorp.dv1.enums;

public enum ForecastCategory {

    ROUNDS("Rondas"),
    VISITS("Visitas"),
    ATTENDANCE("Asistencia"),
    INCIDENCES("Incidencias"),
    EVALUATION("Evaluaciones");
    
    private final String displayName;

    ForecastCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
