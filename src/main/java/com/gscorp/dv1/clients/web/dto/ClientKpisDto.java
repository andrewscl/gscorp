package com.gscorp.dv1.clients.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientKpisDto {

    private Long asistenciaHoy;
    private Long rondasHoy;
    private Long visitasHoy;
    private Long incidentesHoy;
    
}
