package org.tpl.chat.api.dto;

import lombok.Data;
import org.tpl.chat.dal.model.MessageType;

import java.time.LocalDateTime;

@Data
public class PinedMessage {

    private String id;
    private String senderId;
    private MessageType type;
    private String content;
    private LocalDateTime createdDate;
    private String sessionId;

}
