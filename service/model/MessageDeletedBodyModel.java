package org.tpl.chat.service.model;

import lombok.Data;

@Data
public class MessageDeletedBodyModel {
    private String id;
    private String sessionId;
    private MessagePreviewModel previousMessagePreview;
}
