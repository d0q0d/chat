package org.tpl.chat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeenOutputModel {
    private Integer unreadCount;
}
