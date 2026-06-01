package com.gscorp.dv1.services;

import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailTemplateUtils {

    private static final Logger log =
                    LoggerFactory.getLogger(EmailTemplateUtils.class);

    /**
     * Envuelve un fragmento HTML en una estructura válida y le inyecta un archivo CSS.
     * @param fragmentHtml El HTML generado por Thymeleaf (solo los divs del fragmento).
     * @param cssPath La ruta del CSS en resources (ej: "static/css/estilos.css").
     * @return El HTML completo listo para enviar por correo.
     */
    public static String buildStyledEmail(String htmlContent, String cssPath){

        try {
            Document doc = Jsoup.parse(htmlContent);
            doc.outputSettings().charset(StandardCharsets.UTF_8);
            return doc.html();

        } catch (Exception e) {
            log.error("Error al normalizar el correo" , e.getMessage());
            return htmlContent;

        }
    }
}
