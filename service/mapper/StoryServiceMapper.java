package org.tpl.chat.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.tpl.chat.dal.model.Story;

@Mapper(componentModel = "spring")
public interface StoryServiceMapper {
    void update(@MappingTarget Story story, String senderId, String senderRoleCode, String senderOrganizationCode, String senderFormationCode);
}
