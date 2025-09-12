package com.gscorp.dv1.services.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.gscorp.dv1.api.dto.LicitationDTO;
import com.gscorp.dv1.entities.Licitation;

public final class LicitationMapper {
    
    private LicitationMapper(){}

    public static Licitation toEntity(LicitationDTO dto, Licitation target) {

        if (target == null) target = new Licitation();

        target.setExternalCode(dto.getExternalCode());
        target.setName(dto.getName());

        target.setPublishDate(parseFecha(dto.getPublishDate()));
        target.setCloseDate(parseFecha(dto.getCloseDate()));

        if(dto.getBuyer() != null) {
            target.setBuyerName(dto.getBuyer().getName());
            target.setBuyerCode(dto.getBuyer().getCode());
        }

        return target;
    }

    private static LocalDate parseFecha (String raw) {

        if(raw ==null || raw.isBlank()) return null;

        for(DateTimeFormatter f : List.of(
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("ddmmyyyy")
        )) {
            try{
                if(f == DateTimeFormatter.ISO_DATE_TIME){
                    return java.time.LocalDateTime.parse(raw, f).toLocalDate();
                }
                return LocalDate.parse(raw, f);
            } catch (Exception ignored) {}
        }

        return null;
    }
}
