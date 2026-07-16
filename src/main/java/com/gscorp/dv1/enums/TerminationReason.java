package com.gscorp.dv1.enums;

public enum TerminationReason {
    INJUSTIFY_ABSENCE("Ausencias injustificadas"),
    ABANDON("Abandono"),
    CONTRACT_EXPIRED("Término de contrato"),
    VOLUNTARY_QUIT("Renuncia voluntaria"),
    VERBAL_QUIT("Renuncia verbal");

    private final String displayName;

    TerminationReason(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
}
