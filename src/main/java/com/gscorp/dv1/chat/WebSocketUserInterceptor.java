package com.gscorp.dv1.chat;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class WebSocketUserInterceptor implements ChannelInterceptor{

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accesor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(accesor != null && accesor.getSessionAttributes()!=null) {
            String username = (String) accesor.getSessionAttributes().get("username");
            if(username!=null){
                accesor.setUser(new UsernamePasswordAuthenticationToken(username, null, List.of()));
            }
        }

        return message;
    }
    
}
