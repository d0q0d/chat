package org.tpl.chat.dal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "user", timeToLive = 3600)
public class User {
  @Id
  private String userId;
  private String firstName;
  private String lastName;
  private String personnelPhotoUrl;

  public String getFullName(){
    String fullName = "";
    if (Objects.nonNull(firstName)) fullName = fullName + firstName;
    if (Objects.nonNull(lastName)) fullName = fullName + " " + lastName;
    return fullName.trim();
  }

}
