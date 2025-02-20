package org.tpl.chat.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
@Data
public class CreateGroupInputModel {
    @NotBlank
    private String name;
    @NotEmpty
    private Set<String> members;
    private MultipartFile file;
}
