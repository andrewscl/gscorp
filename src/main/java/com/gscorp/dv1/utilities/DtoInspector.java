package com.gscorp.dv1.utilities; // o cualquier package de tu app

import java.util.Arrays;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DtoInspector {
    @PostConstruct
    public void inspect() {
        try {
            Class<?> cls = com.gscorp.dv1.incidents.web.dto.IncidentDto.class;
            System.out.println("IncidentDto class: " + cls.getName());
            System.out.println("Declared constructors:");
            Arrays.stream(cls.getConstructors()).forEach(c -> {
                System.out.println(" ctor: " + Arrays.toString(c.getParameterTypes()));
            });
        } catch (Throwable t) {
            System.out.println("Error inspecting IncidentDto:");
            t.printStackTrace();
        }
    }
}