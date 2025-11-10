package com.gscorp.dv1.enums;

public enum IncidentType {
    
    ACCIDENT("Accidente"),
    THEFT("Robo"),
    INTRUSION("Intrusión"),
    EQUIPMENT_FAILURE("Falla de equipo"),
    FIRE("Incendio"),
    MEDICAL("Emergencia médica"),
    SAFETY_RISK("Riesgo de seguridad"),
    ENVIRONMENTAL("Incidente ambiental"),
    POWER_OUTAGE("Corte de energía"),
    VANDALISM("Vandalismo"),
    SERVICE_OUTAGE("Corte de servicio"),
    VIOLENCE("Violencia"),
    MAINTENANCE("Mantenimiento"),
    OTHER("Otro");

    private final String description;

    IncidentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
