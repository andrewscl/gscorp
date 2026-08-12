package com.gscorp.dv1.users.application.dto;

import java.util.List;

public record ProjectScope (boolean ignoreFilter, 
                            List<Long> projectIds
){
    public static ProjectScope unrestricted () {
        return new ProjectScope(true, List.of());
    }

    public static ProjectScope restricted (List<Long> projectIds){
        return new ProjectScope(false, projectIds != null ? projectIds : List.of());
    }

    public boolean hasNoAccess() {
        return !ignoreFilter && projectIds.isEmpty();
    }
}
