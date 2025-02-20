package org.tpl.chat.api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UserInfoInputModel {
  @NotEmpty
  private Set<String> userIdSet;
}
