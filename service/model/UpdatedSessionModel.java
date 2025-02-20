package org.tpl.chat.service.model;

import lombok.Data;
import org.tpl.chat.dal.model.ExtraInfo;
import org.tpl.chat.dal.model.SessionType;

@Data
public class UpdatedSessionModel {
    private String id;
    private SessionType sessionType;
    private String imageUrl;
    private ExtraInfo extraInfo;
    private Integer memberCount;
}
