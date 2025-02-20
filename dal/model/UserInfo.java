package org.tpl.chat.dal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tpl.util.mongodbcommon.dal.entity.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "userInfo")
public class UserInfo extends BaseEntity {
    @Indexed(unique = true)
    private String userId;
    private OnlineStatusEnum onlineStatus;
    private LocalDateTime lastSeen;
    private Set<PinedSession> pinedSessionsSet;
}
