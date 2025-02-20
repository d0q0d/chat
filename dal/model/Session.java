package org.tpl.chat.dal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.tpl.chat.service.model.MessagePreviewModel;
import org.tpl.chat.service.model.SessionRole;
import org.tpl.util.mongodbcommon.dal.entity.BaseEntity;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Document(collection = "session")
public class Session extends BaseEntity {

  @Indexed private SessionType sessionType;
  @Indexed private Set<String> members;
  private List<SessionDeleteModel> sessionDeleteModelList;
  private String imageUrl;
  private ExtraInfo extraInfo;
  private String pinedMessageId;
  private Boolean isSavedMessages;
  @Transient private Message pinedMessage;
  @Transient private LocalDateTime pinedDateTime;
  @Transient private MessagePreviewModel messagePreview;
  @Transient private Integer unreadMessageCount;
  @Transient private String secondSideUserId;
  @Transient private SessionRole role;
  @Transient private LocalDateTime secondSideLastSeen;
  @Transient private OnlineStatusEnum secondSideOnlineStatus;
  @Transient private Long memberCount;
  @Transient private Long onlineMemberCount;
  @Transient private String firstUnreadMessageId;

  public Session(Set<String> members, SessionType sessionType) {
    this.members = members;
    this.sessionType = sessionType;
  }

  public Session(Set<String> members, SessionType sessionType, Boolean isSavedMessages) {
    this.members = members;
    this.sessionType = sessionType;
    this.isSavedMessages =isSavedMessages;
  }
}
