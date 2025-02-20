package org.tpl.chat.api.dto;

import lombok.Data;
import org.tpl.chat.dal.model.StoryType;
import org.tpl.chat.service.model.MemberModel;

import java.time.LocalDateTime;

@Data
public class StoryOutputModel {
    private String id;
    private String content;
    private StoryType type;
    private String url;
    private LocalDateTime createdDate;
    private MemberModel sender;
    private Boolean seen;
    private Integer receiversCount;
}
