package org.tpl.chat.service.model;

import lombok.Data;
import org.tpl.chat.dal.model.MessageMetaData;
import org.tpl.chat.dal.model.MessageStatus;
import org.tpl.chat.dal.model.MessageType;
import org.tpl.chat.dal.model.Story;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class MessageBodyModel {
    private String id;
    private String senderId;
    private String content;
    private MessageType type;
    private String url;
    private LocalDateTime createdDate;
    private String repliedId;
    private MessagePreviewModel repliedMessagePreview;
    private String sessionId;
    private MessageStatus messageStatus;
    private MemberModel sender;
    private Map<String ,String> reactions;
    private StoryModel story;
    private String storyRepliedId;
    private MessageMetaData messageMetaData;
}
