package org.tpl.chat.service.model;

import lombok.Data;
import org.tpl.chat.dal.model.MessageType;

import java.time.LocalDateTime;

@Data
public class MessagePreviewModel {

    private String messageId;
    private String content;
    private MessageType type;
    private LocalDateTime createdDate;

}
