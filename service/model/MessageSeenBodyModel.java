package org.tpl.chat.service.model;

import lombok.Data;
import org.tpl.chat.dal.model.MessageStatus;

@Data
public class MessageSeenBodyModel {
    private String id;
    private String sessionId;
    private MessageStatus messageStatus;
}
