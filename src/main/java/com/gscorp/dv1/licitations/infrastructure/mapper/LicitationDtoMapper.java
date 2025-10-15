package com.gscorp.dv1.licitations.infrastructure.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.gscorp.dv1.licitations.infrastructure.Licitation;
import com.gscorp.dv1.licitations.web.dto.LicitationDto;

public final class LicitationDtoMapper {

    private LicitationDtoMapper() {}

    public static Licitation toEntity(LicitationDto dto, Licitation target) {
        if (target == null) target = new Licitation();

        target.setExternalCode(dto.externalCode());
        target.setName(dto.name());
        target.setDescription(dto.description());
        target.setStatus(dto.status());
        target.setType(dto.type());
        target.setBuyerName(dto.buyerName());
        target.setBuyerRut(dto.buyerRut());
        target.setPublishDate(parseFecha(dto.publishDate()));
        target.setCloseDate(parseFecha(dto.closeDate()));
        target.setOpenDate(parseFecha(dto.openDate()));
        target.setAwardDate(parseFecha(dto.awardDate()));
        target.setEstimatedAmount(dto.estimatedAmount());
        target.setCurrency(dto.currency());
        target.setCategory(dto.category());
        target.setSector(dto.sector());
        target.setSubCategory(dto.subCategory());
        target.setRegion(dto.region());
        target.setCommune(dto.commune());
        target.setContactName(dto.contactName());
        target.setContactEmail(dto.contactEmail());
        target.setContactPhone(dto.contactPhone());
        target.setBasesUrl(dto.basesUrl());
        target.setRecordUrl(dto.recordUrl());
        target.setJsonData(dto.jsonData());
        // Relaciones con items y adjudicados, si necesitas mapearlos:
        // target.setItems( ... );
        // target.setAwarded( ... );
        // El campo lastSync se setea fuera del mapper (en el sync service normalmente)

        return target;
    }

    private static Date parseFecha(String raw) {
        if (raw == null || raw.isBlank()) return null;

        for (DateTimeFormatter f : java.util.List.of(
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("ddMMyyyy")
        )) {
            try {
                if (f == DateTimeFormatter.ISO_DATE_TIME) {
                    LocalDateTime ldt = LocalDateTime.parse(raw, f);
                    return java.util.Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant());
                }
                LocalDate ld = LocalDate.parse(raw, f);
                return java.util.Date.from(ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            } catch (Exception ignored) {}
        }
        return null;
    }
}