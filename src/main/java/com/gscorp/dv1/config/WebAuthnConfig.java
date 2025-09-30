package com.gscorp.dv1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.util.ObjectConverter;

@Configuration
public class WebAuthnConfig {

    @Bean
    public ObjectConverter objectConverter(){
        return new ObjectConverter();
    }
    
    @Bean
    public WebAuthnManager webAuthnManager (ObjectConverter objectConverter) {
        //Non-strict para arrancar rapido, se pueden endurecer validaciones mas adelante
        return WebAuthnManager.createNonStrictWebAuthnManager(objectConverter);
    }

}
