package org.tpl.chat.dal.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
public class Channel {
  private String name;
  private String description;
  private String imageUrl;
  private MultipartFile file;
  private String userId;
  private String sessionId;
  private Set<String> members;
  private Set<String> owners;
}
