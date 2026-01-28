package com.gscorp.dv1.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Integer>{
    
    @Override
    public Integer convertToDatabaseColumn(DayOfWeek dayOfWeek){
        //Almacenar el numero correspondiente en la base de datos
        return (dayOfWeek != null) ? dayOfWeek.getDayNumber() : null;
    }

    @Override
    public DayOfWeek convertToEntityAttribute(Integer dayNumber) {
        //Recuperar el enum correspondiente basado en el numero almacenado
        return dayNumber != null ? DayOfWeek.fromDayNumber(dayNumber) : null;
    }

}
