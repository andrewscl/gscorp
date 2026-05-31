package com.gscorp.dv1.services;

import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class EmailTemplateUtils {

    private static final Logger log =
                    LoggerFactory.getLogger(EmailTemplateUtils.class);

    /**
     * Envuelve un fragmento HTML en una estructura válida y le inyecta un archivo CSS.
     * @param fragmentHtml El HTML generado por Thymeleaf (solo los divs del fragmento).
     * @param cssPath La ruta del CSS en resources (ej: "static/css/estilos.css").
     * @return El HTML completo listo para enviar por correo.
     */
    public static String buildStyledEmail(String fragmentHtml, String cssPath){

        // Estructura obligatoria de envio de correo
        String fullHtmlStructure = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background-color:#f7f7f9;">
                %s
            </body>
            </html>
            """.formatted(fragmentHtml);

        // Parsear con Jsoup
        Document doc = Jsoup.parse(fullHtmlStructure);

        try {
            //Cargar el css compilado desde los recursos
            ClassPathResource cssResource = new ClassPathResource(cssPath);
            String cssContent =
                        new String(cssResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            doc.head().append("<style>" + cssContent + "</style>");
        } catch (Exception e) {
            log.error(
            "No se pudieron acoplar los estilos SCSS al correo. Se enviará el formato plano: {}"
                                                                                        , e.getMessage());
        }
        return doc.html();
    }

}
