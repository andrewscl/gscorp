package com.gscorp.dv1.sites.web.dto;

import com.gscorp.dv1.clients.infrastructure.Client;

public record SiteDto (Client client,Long id, String name, String code, String address){
    
}
