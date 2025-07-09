package com.gscorp.dv1.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import jakarta.annotation.PostConstruct;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@SuppressWarnings("deprecation")// se requiere JacksonFactoryahora 
@Service
public class GmailService {
    
    private static final String APPLICATION_NAME = "MiAppsContactos";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(GmailScopes.GMAIL_SEND);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private Gmail gmail;

    @PostConstruct
    public void init() {
        try {
            String credPath = System.getenv("GOOGLE_CREDENTIALS");
            InputStream in = new FileInputStream(credPath);
            GoogleClientSecrets clientSecrets = 
                                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleAuthorizationCodeFlow flow =
                                new GoogleAuthorizationCodeFlow.Builder(
                                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                                    .setAccessType("offline")
                                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.
                                                    Builder()
                                                        .setPort(8888)
                                                        .build();            
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            this.gmail = new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException | GeneralSecurityException e){
            throw new RuntimeException("Fail to initialize GmailService: " + e.getMessage(), e);
        }
    }

    @Async
    public void sendMail(String to, String subject, String htmlBody) throws Exception {
        
        //Crea un correo
        MimeMessage email = createEmail(to, "me", subject, htmlBody);
        //Convierte a formato message para la GMail API
        com.google.api.services.gmail.model.Message message = createMessageWithMail(email);
        //envia el correo utilizando la API de Gmail.
        gmail.users().messages().send("me", message).execute();
    }

    private MimeMessage createEmail(String to, String from, String subject, String htmlBody) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject, "UTF-8");
        email.setContent(htmlBody, "text/html; charset=utf-8");
        return email;
    }


    private com.google.api.services.gmail.model.Message createMessageWithMail(MimeMessage email) throws IOException, MessagingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());
        com.google.api.services.gmail.model.Message message = 
                            new com.google.api.services.gmail.model.Message();
        message.setRaw(encodedEmail);
        return message;
    }

}
