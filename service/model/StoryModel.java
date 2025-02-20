package org.tpl.chat.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tpl.chat.dal.model.StoryType;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryModel {
    @Field("id")
    private String id;
    private String content;
    private StoryType type;
    private String url;
    private LocalDateTime createdDate;

}
