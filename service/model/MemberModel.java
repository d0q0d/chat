package org.tpl.chat.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tpl.chat.dal.model.OnlineStatusEnum;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberModel {
    @Field("id")
    private String id;
    private String imageUrl;
    private String name;
    private SessionRole role;
    private LocalDateTime lastSeen;
    private OnlineStatusEnum onlineStatus;

    public MemberModel(String id, String imageUrl, String name, SessionRole role) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.role = role;
    }
}
