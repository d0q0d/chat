package org.tpl.chat.api.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateGroupInputModel {
    private String name;
    private String description;
    private Set<String> addMemberSet;
    private Set<String> removeMemberSet;
    private Set<String> addOwnerSet;
    private Set<String> removeOwnerSet;
}
