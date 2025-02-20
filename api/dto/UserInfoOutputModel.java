package org.tpl.chat.api.dto;

import lombok.Data;
import org.tpl.chat.dal.model.OnlineStatusEnum;

import java.time.LocalDateTime;

@Data
public class UserInfoOutputModel {
    private String userId;
    private OnlineStatusEnum onlineStatus;
    private LocalDateTime lastSeen;
}
