package org.tpl.chat.service.remote.model;

import lombok.Data;

@Data
public class UserManagementProfile {
  private String id;
  private String firstName;
  private String lastName;
  private String personnelPhotoUrl;
}
