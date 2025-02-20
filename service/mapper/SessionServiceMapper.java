package org.tpl.chat.service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tpl.chat.dal.model.Message;
import org.tpl.chat.dal.model.Session;
import org.tpl.chat.dal.model.User;
import org.tpl.chat.service.model.MessagePreviewModel;
import org.tpl.chat.service.model.SocketMessageModel;
import org.tpl.chat.service.model.UpdateSessionModel;

@Mapper(componentModel = "spring")
public interface SessionServiceMapper {

    @Mapping(target = "imageUrl", source = "personnelPhotoUrl")
    @Mapping(target = "extraInfo.name", expression = "java(user.getFullName())")
    @BeanMapping(ignoreByDefault = true)
    void setInfoForP2P(@MappingTarget Session session, User user);

    @Mapping(target = "messageId", source = "id")
    MessagePreviewModel getMessagePreviewModelFromMessage(Message message);

    @Mapping(target = "data.sessionId", source = "sessionId")
    @Mapping(target = "type", expression = "java(SocketMessageModel.Type.SESSION_DELETED)")
    @Mapping(target = "id", ignore = true)
    SocketMessageModel<UpdateSessionModel> getSessionDeletedModel(String sessionId);
}
