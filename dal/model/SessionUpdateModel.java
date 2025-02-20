package org.tpl.chat.dal.model;

import lombok.Data;

import java.util.Set;

@Data
public class SessionUpdateModel {
  private String sessionId;
  private String name;
  private String description;
  private Set<String> addMemberSet;
  private Set<String> removeMemberSet;
  private Set<String> addOwnerSet;
  private Set<String> removeOwnerSet;
}
