package com.gscorp.dv1.chat;

import java.security.Principal;
import java.time.Instant;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    
    @MessageMapping("/send-message")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(@Payload ChatMessage message, Principal principal) {
        
        if(principal != null) {
            message.setFrom(principal.getName());
        }

        if(message.getTimestamp() == null) {
            message.setTimestamp(Instant.now().toString());
        }

        return message;
    }
}
