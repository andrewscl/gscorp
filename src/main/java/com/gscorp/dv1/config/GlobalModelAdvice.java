package com.gscorp.dv1.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void addGlobalAttributes(Model model) {

        String googleCloudApiKey = System.getenv("GOOGLE_CLOUD_API_KEY");
        String googleMapId = System.getenv("GOOGLE_MAP_ID");

        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("googlemapid", googleMapId);

    }

}
