package org.tpl.chat.api.facade.mapper;

import org.mapstruct.Mapper;
import org.tpl.chat.api.dto.MessageOutputModel;
import org.tpl.chat.dal.model.Message;
import org.tpl.chat.dal.model.MessageType;

@Mapper(componentModel = "spring")
public interface MessageFacadeMapper {



    Message getEntity(
            String senderId, String sessionId, MessageType type, String repliedId, String content, String storyRepliedId);

    MessageOutputModel getDto(Message message);

    MessageOutputModel getDtoFromMessageAndRequestId(Message message, String requestId);


}
