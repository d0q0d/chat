package org.tpl.chat.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadModel {
    private String url;
    private String previewUrl;
    private Integer duration;
}
