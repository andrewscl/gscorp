package com.gscorp.dv1.services;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;


/**
 * Servicio WhatsApp simplificado usando WebClient.
 *
 * Hecho para evitar problemas de inferencia de tipos con onStatus(...).
 * En lugar de usar retrieve().onStatus(...), aquí uso exchangeToMono(...) y
 * compruebo el status explícitamente, devolviendo error con el body cuando aplica.
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${app.public-base-url:https://localhost}")
    private String appBaseUrl;

    private WebClient client;
    private URI endpointUri;

    @PostConstruct
    public void init() {
        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            throw new IllegalStateException("whatsapp.phone-number-id must be provided");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("whatsapp.access-token must be provided");
        }

        this.endpointUri = URI.create("https://graph.facebook.com/v16.0/" + phoneNumberId + "/messages");

        this.client = WebClient.builder()
                .baseUrl(endpointUri.toString())
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("WhatsAppService initialized for phoneNumberId={}", phoneNumberId);
    }

    @Async
    public void sendTextMessage(String toE164, String text) {
        try {
            sendTextMessageSync(toE164, text);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp text to {}: {}", toE164, e.getMessage(), e);
        }
    }

    /**
     * Versión síncrona usando exchangeToMono para manejar status y body sin problemas de inferencia.
     */
    public Map<String, Object> sendTextMessageSync(String toE164, String text) throws Exception {
        if (!StringUtils.hasText(toE164)) {
            throw new IllegalArgumentException("toE164 is required");
        }

        var body = Map.<String, Object>of(
                "messaging_product", "whatsapp",
                "to", toE164,
                "type", "text",
                "text", Map.of("body", text)
        );

        ParameterizedTypeReference<Map<String, Object>> mapType = new ParameterizedTypeReference<>() {};

        return client.post()
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchangeToMono((ClientResponse response) -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("WhatsApp API error: " + b)));
                    }
                    return response.bodyToMono(mapType);
                })
                .timeout(Duration.ofSeconds(10))
                .block();
    }

    @Async
    public void sendTemplateMessage(String toE164, String templateName, String languageCode, List<String> bodyParameters) {
        try {
            sendTemplateMessageSync(toE164, templateName, languageCode, bodyParameters);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp template to {}: {}", toE164, e.getMessage(), e);
        }
    }

    public Map<String, Object> sendTemplateMessageSync(String toE164, String templateName, String languageCode, List<String> bodyParameters) throws Exception {
        var parameters = bodyParameters == null ? List.of() :
                bodyParameters.stream()
                        .map(p -> Map.<String, Object>of("type", "text", "text", p))
                        .toList();

        var component = Map.<String, Object>of(
                "type", "body",
                "parameters", parameters
        );

        var template = Map.<String, Object>of(
                "name", templateName,
                "language", Map.of("code", languageCode),
                "components", List.of(component)
        );

        var body = Map.<String, Object>of(
                "messaging_product", "whatsapp",
                "to", toE164,
                "type", "template",
                "template", template
        );

        ParameterizedTypeReference<Map<String, Object>> mapType = new ParameterizedTypeReference<>() {};

        return client.post()
                .bodyValue(body)
                .exchangeToMono((ClientResponse response) -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("WhatsApp API error: " + b)));
                    }
                    return response.bodyToMono(mapType);
                })
                .timeout(Duration.ofSeconds(10))
                .block();
    }

    @Async
    public void sendMagicLink(String toE164, String displayName, String rawToken) {
        if (!StringUtils.hasText(toE164) || !StringUtils.hasText(rawToken)) {
            log.warn("sendMagicLink called with missing args");
            return;
        }
        String link = buildMagicLink(rawToken);
        String namePart = (displayName != null && !displayName.isBlank()) ? displayName : "";
        String text = String.format("Hola %s, activa tu acceso aquí (expira en 24h): %s", namePart, link);
        sendTextMessage(toE164, text);
    }

    public String buildMagicLink(String token) {
        String base = (appBaseUrl != null) ? appBaseUrl.replaceAll("/+$", "") : "";
        return base + "/auth/define-password?token=" + token;
    }

    public String createToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }
}