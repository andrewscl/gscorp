package com.gscorp.dv1.services;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

        // Limpieza profunda: Quitar saltos de línea, comentarios y mapas de origen de SCSS
        cssContent = cssContent.replaceAll("(?s)/\\*.*?\\*/", "")
                               .replaceAll("\r|\n", " ")
                               .replaceAll("\\s+", " ")
                               .trim();
        
        // 3. Expresión regular robusta para capturar: selector { propiedades }
        Pattern pattern = Pattern.compile("([^\\{\\}]+)\\{([^\\{\\}]+)\\}");
        Matcher matcher = pattern.matcher(cssContent);
        
        while (matcher.find()) {
            String selector = matcher.group(1).trim();
            String styles = matcher.group(2).trim();
            
            // Ignorar selectores web inválidos en emails (como @media o vacíos)
            if (selector.isEmpty() || selector.startsWith("@")) {
                continue;
            }
            try {
                // Seleccionar los elementos del HTML usando el selector exacto de tu SCSS
                Elements elements = doc.select(selector);
                
                for (Element element : elements) {
                    String existingStyles = element.attr("style");
                    
                    // Concatenar los estilos en el atributo inline style="..."
                    if (existingStyles != null && !existingStyles.isEmpty()) {
                        // Aseguramos que termine en punto y coma antes de concatenar
                        if (!existingStyles.endsWith(";")) existingStyles += ";";
                        element.attr("style", existingStyles + " " + styles);
                    } else {
                        element.attr("style", styles);
                    }
                }
            } catch (Exception selectorException) {
                // Si el selector tiene pseudo-clases como :hover, Jsoup saltará pacíficamente aquí
            }
        }

        } catch (Exception e) {
            log.error(
            "No se pudieron acoplar los estilos SCSS al correo. Se enviará el formato plano: {}"
                                                                                        , e.getMessage());
        }
        return doc.html();
    }

}
