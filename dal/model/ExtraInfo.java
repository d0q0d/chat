package org.tpl.chat.dal.model;

import lombok.Data;

import java.util.Set;

@Data
public class ExtraInfo {

  private String name;
  private String description;
  private String creatorId;
  private Set<String> owners;

  public ExtraInfo(){}

  public ExtraInfo(String name, String description, Set<String> owners, String creatorId) {
    this(name, description, owners);
    this.creatorId = creatorId;
  }

  public ExtraInfo(String name, String description, Set<String> owners) {
    this.name = name;
    this.description = description;
    this.owners = owners;
  }

  public ExtraInfo(String name, Set<String> owners) {
    this.name = name;
    this.owners = owners;
  }
}
