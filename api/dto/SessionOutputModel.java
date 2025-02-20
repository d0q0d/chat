package org.tpl.chat.api.dto;

import lombok.Data;
import org.tpl.chat.dal.model.ExtraInfo;
import org.tpl.chat.dal.model.OnlineStatusEnum;
import org.tpl.chat.dal.model.SessionType;
import org.tpl.chat.service.model.MessagePreviewModel;
import org.tpl.chat.service.model.SessionRole;

import java.time.LocalDateTime;

@Data
public class SessionOutputModel {
  private String id;
  private SessionType sessionType;
  private MessagePreviewModel messagePreview;
  private String imageUrl;
  private ExtraInfo extraInfo;
  private Long memberCount;
  private Long onlineMemberCount;
  private Integer unreadMessageCount;
  private String secondSideUserId;
  private SessionRole role;
  private PinedMessage pinedMessage;
  private LocalDateTime pinedDateTime;
  private LocalDateTime secondSideLastSeen;
  private OnlineStatusEnum secondSideOnlineStatus;
  private Boolean isSavedMessages;
  private String firstUnreadMessageId;
}
