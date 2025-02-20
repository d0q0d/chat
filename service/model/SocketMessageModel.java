package org.tpl.chat.service.model;

import lombok.Data;

@Data
public class SocketMessageModel<T> {

  private String id;
  private String receiverId;
  private Type type;
  private T data;

  public enum Type {
    MESSAGE,
    SEEN,
    REACTION,
    INVALID_TOKEN,
    DELETED,
    EDITED,
    SESSION_DELETED,
    SESSION_EDITED,
    PING,
    ACKNOWLEDGE,
    PONG,
    IS_TYPING,
    ADDED_TO_OWNERS,
    REMOVED_FROM_OWNERS,
    ADDED_TO_MEMBERS,
    REMOVED_FROM_MEMBERS
  }

}
