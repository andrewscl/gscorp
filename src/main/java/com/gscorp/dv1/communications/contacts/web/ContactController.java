package com.gscorp.dv1.communications.contacts.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.communications.contacts.application.ContactService;
import com.gscorp.dv1.communications.contacts.infrastructure.Contact;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public/api")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

        @PostMapping("/contact")
        public ResponseEntity<?> handleContactForm(@RequestBody Contact contact) {
            try {
                contactService.saveContact(contact);
                return ResponseEntity.
                        ok("Gracias por contactarnos. Hemos recibido su mensaje y nos pondremos en contacto a la brevedad.");
            } catch (Exception e) {
                return ResponseEntity.status(500).
                        body("Error al guardar el formulario");
            }

        }

}
