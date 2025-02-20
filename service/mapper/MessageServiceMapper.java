package org.tpl.chat.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tpl.chat.dal.model.Message;
import org.tpl.chat.service.model.*;

@Mapper(componentModel = "spring")
public interface MessageServiceMapper {

    @Mapping(target = "data", source = "message")
    @Mapping(target = "type", expression = "java(SocketMessageModel.Type.MESSAGE)")
    @Mapping(target = "id", ignore = true)
    SocketMessageModel<MessageBodyModel> messageToSocketMessageModel(Message message);

    @Mapping(target = "data", source = "message")
    @Mapping(target = "type", expression = "java(SocketMessageModel.Type.SEEN)")
    @Mapping(target = "id", ignore = true)
    SocketMessageModel<MessageSeenBodyModel> messageToSocketSeenModel(Message message);

    @Mapping(target = "data", source = "message")
    @Mapping(target = "data.previousMessagePreview.messageId", source = "lastMessage.id")
    @Mapping(target = "data.previousMessagePreview.content", source = "lastMessage.content")
    @Mapping(target = "data.previousMessagePreview.type", source = "lastMessage.type")
    @Mapping(target = "data.previousMessagePreview.createdDate", source = "lastMessage.createdDate")
    @Mapping(target = "type", expression = "java(SocketMessageModel.Type.DELETED)")
    @Mapping(target = "id", ignore = true)
    SocketMessageModel<MessageDeletedBodyModel> messageToSocketDeletedModel(Message message, Message lastMessage);

    @Mapping(target = "data", source = "message")
    @Mapping(target = "type", expression = "java(SocketMessageModel.Type.EDITED)")
    @Mapping(target = "id", ignore = true)
    SocketMessageModel<MessageBodyModel> messageToSocketEditedModel(Message message);

    @Mapping(target = "data.reactionSenderId", source = "reactionSenderId")
    @Mapping(target = "data.reaction", source = "reaction")
    @Mapping(target = "data.id", source = "id")
    @Mapping(target = "data.sessionId", source = "sessionId")
    @Mapping(target = "type", expression = "java(SocketMessageModel.Type.REACTION)")
    @Mapping(target = "id", ignore = true)
    SocketMessageModel<MessageReactionModel> messageToSocketReactionModel(String reactionSenderId, String reaction, String id, String sessionId);

    @Mapping(target = "data.userId", source = "userId")
    @Mapping(target = "data.sessionId", source = "sessionId")
    @Mapping(target = "data.name", source = "name")
    @Mapping(target = "data.imageUrl", source = "imageUrl")
    @Mapping(target = "type", expression = "java(SocketMessageModel.Type.IS_TYPING)")
    @Mapping(target = "id", ignore = true)
    SocketMessageModel<MessageTypingModel> messageToSocketTypingModel(String userId, String sessionId, String name, String imageUrl);

    MessagePreviewModel getMessagePreviewModelFromMessage(Message message);

}
