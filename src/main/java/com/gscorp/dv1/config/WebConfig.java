package com.gscorp.dv1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Employee photos
        registry.addResourceHandler("/files/employee_photos/**")
                .addResourceLocations("file:///C:/gscorp_uploads/employee_photos/");

        // Supervision files
        registry.addResourceHandler("/files/supervision_files/**")
                .addResourceLocations("file:///C:/gscorp_uploads/supervision_files/");

        // Administration files
        registry.addResourceHandler("/files/administration_files/**")
                .addResourceLocations("file:///C:/gscorp_uploads/administration_files/");
    }

}
