package org.tpl.chat.dal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "liveMessage", timeToLive = 600)
public class LiveMessage {
    @Id
    private String id;
    private LocalDateTime dateTime;
    private String topic;
    private String message;
}