package com.gscorp.dv1.communications.chat;

import lombok.Data;

@Data
public class ChatMessage {
    
    private String from;
    private String content;
    private String timestamp;
}
