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
    public static String buildStyledEmail(String htmlContent, String cssPath){

        Document doc = Jsoup.parse(htmlContent);

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
