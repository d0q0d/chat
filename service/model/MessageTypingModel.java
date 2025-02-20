package org.tpl.chat.service.model;

import lombok.Data;

@Data
public class MessageTypingModel {
    private String userId;
    private String sessionId;
    private String imageUrl;
    private String name;
}
