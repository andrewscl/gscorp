package com.gscorp.dv1.enums;

public enum ForecastMetric {

    ROUNDS("Rondas"),
    VISITS("Visitas"),
    ATTENDANCE("Asistencia"),
    INCIDENCES("Incidencias"),
    EVALUATION("Evaluaciones");
    
    private final String displayName;

    ForecastMetric(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
