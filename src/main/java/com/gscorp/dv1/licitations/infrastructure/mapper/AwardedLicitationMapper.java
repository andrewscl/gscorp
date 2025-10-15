package com.gscorp.dv1.licitations.infrastructure.mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gscorp.dv1.licitations.infrastructure.AwardedLicitation;
import com.gscorp.dv1.licitations.infrastructure.Licitation;
import com.gscorp.dv1.licitations.web.dto.AwardedLicitationDto;

public final class AwardedLicitationMapper {

    private AwardedLicitationMapper() {}

    public static AwardedLicitation toEntity(AwardedLicitationDto dto, Licitation licitation) {
        AwardedLicitation entity = new AwardedLicitation();
        entity.setSupplierName(dto.supplierName());
        entity.setSupplierRut(dto.supplierRut());
        entity.setAmount(dto.amount());
        entity.setCurrency(dto.currency());
        entity.setAwardDate(parseFecha(dto.awardDate())); // Si necesitas convertir la fecha, hazlo aquí
        entity.setLicitation(licitation); // relación padre
        return entity;
    }

    private static Date parseFecha(String raw) {
        if (raw == null || raw.isBlank()) return null;

        // Puedes ajustar los formatos según lo que recibas en el JSON
        String[] formatos = { "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd", "dd/MM/yyyy" };

        for (String formato : formatos) {
            try {
                return new java.text.SimpleDateFormat(formato).parse(raw);
            } catch (java.text.ParseException ignored) {}
        }

        return null;
    }

    public static List<AwardedLicitation> toEntityList(List<AwardedLicitationDto> dtos, Licitation licitation) {
        if (dtos == null) return null;
        List<AwardedLicitation> awardedList = new ArrayList<>();
        for (AwardedLicitationDto dto : dtos) {
            awardedList.add(toEntity(dto, licitation));
        }
        return awardedList;
    }

}
