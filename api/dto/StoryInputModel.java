package org.tpl.chat.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.tpl.chat.dal.model.StoryType;
import org.springframework.web.multipart.MultipartFile;

@Data
public class StoryInputModel {
    private String content;
    @NotNull(message = "{general.not.null}")
    private StoryType type;
    private MultipartFile file;
}
