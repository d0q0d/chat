package org.tpl.chat.api.facade.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tpl.chat.api.dto.UpdateGroupInputModel;
import org.tpl.chat.dal.model.Group;
import org.tpl.chat.dal.model.SessionUpdateModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface GroupFacadeMapper {
    @Mapping(source = "name" , target = "name")
    Group getGroupModel(String name, Set<String> members, MultipartFile file, String userId);
    @Mapping(source = "inputModel.name" , target = "name")
    SessionUpdateModel updateGroupInputModelToSessionUpdateModel(UpdateGroupInputModel inputModel, String sessionId);
}
