package org.tpl.chat.api.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateImageInputModel {
    private MultipartFile file;
}
