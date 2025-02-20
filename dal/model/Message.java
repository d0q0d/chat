package org.tpl.chat.dal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.service.model.MessagePreviewModel;
import org.tpl.util.mongodbcommon.dal.entity.BaseEntity;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@CompoundIndexes({
        @CompoundIndex(name = "createdDate", def = "{createdDate:1}"),
        @CompoundIndex(name = "createdDateSessionIdIndex", def = "{createdDate:1, sessionId:1}", unique = true)
})
@Document(collection = "message")
public class Message extends BaseEntity {

  @Indexed
  private String sessionId;
  private String senderId;
  private String content;
  private String repliedId;
  private String storyRepliedId;
  private MessageType type;
  private String url;
  private List<String> receiverIds;
  private MessageStatus messageStatus;
  private Map<String, String> reactions;
  private MessageMetaData messageMetaData;
  @Transient
  private MemberModel sender;
  @Transient
  private Story story;
  @Transient
  private MessagePreviewModel repliedMessagePreview;

  public Message(String sessionId, String senderId, MessageType type) {
    this.sessionId = sessionId;
    this.senderId = senderId;
    this.type = type;
  }
}
