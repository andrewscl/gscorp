package com.gscorp.dv1.enums;

public enum EmployeeStatus {
    HIRED ("Por iniciar"),
    ACTIVE ("Activo"),
    NOTICE_GIVEN ("Aviso de termino"),
    INACTIVE ("Desvinculado"),
    SETTLED ("Cerrado");

    private final String displayName;

    EmployeeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
    
}
