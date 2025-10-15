package com.gscorp.dv1.licitations.infrastructure.mapper;

import java.util.ArrayList;
import java.util.List;

import com.gscorp.dv1.licitations.infrastructure.Licitation;
import com.gscorp.dv1.licitations.infrastructure.ItemLicitation;
import com.gscorp.dv1.licitations.web.dto.ItemLicitationDto;

public final class ItemLicitationMapper {

    private ItemLicitationMapper() {}

    public static ItemLicitation toEntity(ItemLicitationDto dto, Licitation licitation) {
        ItemLicitation entity = new ItemLicitation();
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setQuantity(dto.quantity());
        entity.setUnit(dto.unit());
        entity.setEstimatedAmount(dto.estimatedAmount());
        entity.setCurrency(dto.currency());
        entity.setCategory(dto.category());
        entity.setSubCategory(dto.subCategory());
        entity.setLicitation(licitation); // relación padre
        return entity;
    }

    public static List<ItemLicitation> toEntityList(List<ItemLicitationDto> dtos, Licitation licitation) {
        if (dtos == null) return null;
        List<ItemLicitation> items = new ArrayList<>();
        for (ItemLicitationDto dto : dtos) {
            items.add(toEntity(dto, licitation));
        }
        return items;
    }
}