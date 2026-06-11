package com.gscorp.dv1.enums;

public enum EmployeeStatus {
    HIRED ("Activo"),
    NOTICE_GIVEN ("Aviso de termino"),
    INACTIVE ("Desvinculado");

    private final String displayName;

    EmployeeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
    
}
