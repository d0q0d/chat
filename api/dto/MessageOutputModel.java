package org.tpl.chat.api.dto;

import lombok.Data;
import org.tpl.chat.dal.model.MessageMetaData;
import org.tpl.chat.dal.model.MessageStatus;
import org.tpl.chat.dal.model.MessageType;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.service.model.MessagePreviewModel;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class MessageOutputModel {
  private String id;
  private String senderId;
  private String content;
  private MessageType type;
  private String url;
  private LocalDateTime createdDate;
  private String repliedId;
  private MessagePreviewModel repliedMessagePreview;
  private String storyRepliedId;
  private StoryOutputModel story;
  private String sessionId;
  private MessageStatus messageStatus;
  private String requestId;
  private MemberModel sender;
  private Map<String, String> reactions;
  private MessageMetaData messageMetaData;
}
