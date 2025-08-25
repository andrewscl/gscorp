package com.gscorp.dv1.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.repositories.ContactRepository;
import com.gscorp.dv1.entities.Contact;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final GmailService gmailService;

    public Optional <Contact> findByEmail(String email){
        return contactRepository.findByEmail(email);
    }

    public void saveContact (Contact contact){
        contactRepository.save(contact);

        //enviar notificacion por correo
        String subject = "Nuevo mensaje desde formulario de contacto";
        String htmlBody = """
                <h2>Nuevo mensaje recibido</h2>
                <p><strong>Nombre:</strong> %s</p>
                <p><strong>Telefono:</strong> %s</p>
                <p><strong>Correo:</strong> %s</p>
                <p><strong>Mensaje:</strong> %s</p>
                """.formatted(contact.getName(),
                                contact.getPhone(), 
                                contact.getEmail(), 
                                contact.getMessage());

        try {
            //correo real de la empresa
            gmailService.sendMail("contacto@gscorp.cl", subject, htmlBody);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo.");
        }
    }

}
