package org.tpl.chat.api.facade.mapper;

import org.mapstruct.Mapper;
import org.tpl.chat.api.dto.CreateChannelInputModel;
import org.tpl.chat.api.dto.UpdateChannelInfoInputModel;
import org.tpl.chat.dal.model.Channel;
import org.tpl.chat.dal.model.SessionUpdateModel;

@Mapper(componentModel = "spring")
public interface ChannelFacadeMapper {
  Channel createChannelInputModelToChannel(CreateChannelInputModel inputModel, String userId);
  SessionUpdateModel updateChannelInputModelToChannel(UpdateChannelInfoInputModel inputModel, String sessionId);
}
