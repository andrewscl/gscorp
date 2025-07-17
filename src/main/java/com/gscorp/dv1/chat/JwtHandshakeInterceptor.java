package com.gscorp.dv1.chat;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.gscorp.dv1.security.JwtService;

public class JwtHandshakeInterceptor implements HandshakeInterceptor{
    
    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        
        if(request instanceof ServletServerHttpRequest servletRequest) {
            String  token = servletRequest.getServletRequest().getParameter("token");

            if(token != null && jwtService.isTokenValid(token)) {
                String username = jwtService.extractUsername(token);
                attributes.put("username", username);
            } else {
                String anonId = UUID.randomUUID().toString().substring(0,8);
                attributes.put("username", "anon-" + anonId);
            }
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake (ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception){
                                }

}
