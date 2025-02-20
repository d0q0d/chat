package org.tpl.chat.dal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDeleteModel {
  private String userId;
  private LocalDateTime lastDeletedDate;
  private Boolean isDeleted = false;
}
