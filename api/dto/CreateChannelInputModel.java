package org.tpl.chat.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
public class CreateChannelInputModel {
  @NotBlank
  private String name;
  private String description;
  private Set<String> members;
  private MultipartFile file;
}
