package org.tpl.chat.service.model;

import lombok.Data;

@Data
public class MessageReactionModel {
    private String id;
    private String sessionId;
    private String reactionSenderId;
    private String reaction;
}
