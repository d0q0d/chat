package org.tpl.chat.dal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PinedSession {
    private String sessionId;
    private LocalDateTime pinedDateTime;
}
