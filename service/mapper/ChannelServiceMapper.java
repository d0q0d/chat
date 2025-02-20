package org.tpl.chat.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tpl.chat.dal.model.*;
import org.tpl.chat.service.model.UpdatedSessionModel;
import org.tpl.chat.service.model.SocketMessageModel;
import org.tpl.chat.service.model.UpdateSessionModel;

import java.util.HashSet;
import java.util.Set;

import static org.tpl.chat.dal.model.SessionType.CHANNEL;

@Mapper(componentModel = "spring")
public interface ChannelServiceMapper {

  Session channelToSession(Channel channel);

  default Session channelToSessionWithAdditionalInfo(Channel channel) {
    var session = channelToSession(channel);
    session.setSessionType(CHANNEL);
    if (session.getMembers() == null) session.setMembers(new HashSet<>());
    session.getMembers().add(channel.getUserId());
    session.setExtraInfo(
        new ExtraInfo(
            channel.getName(),
            channel.getDescription(),
            Set.of(channel.getUserId()),
            channel.getUserId()));
    return session;
  }

  default Session updateSession(Session session, SessionUpdateModel sessionUpdateModel) {
    var members = session.getMembers();
    var owners = session.getExtraInfo().getOwners();
    var extraInfo = session.getExtraInfo();

    if (sessionUpdateModel.getDescription() != null) {
      extraInfo.setDescription(sessionUpdateModel.getDescription());
      session.setExtraInfo(extraInfo);
    }
    if (sessionUpdateModel.getName() != null) {
      extraInfo.setName(sessionUpdateModel.getName());
      session.setExtraInfo(extraInfo);
    }
    if (sessionUpdateModel.getAddMemberSet() != null) {
      members.addAll(sessionUpdateModel.getAddMemberSet());
    }
    if (sessionUpdateModel.getAddOwnerSet() != null) {
      owners.addAll(sessionUpdateModel.getAddOwnerSet());
    }
    if (sessionUpdateModel.getRemoveMemberSet() != null) {
      sessionUpdateModel.getRemoveMemberSet().remove(session.getExtraInfo().getCreatorId());
      members.removeAll(sessionUpdateModel.getRemoveMemberSet());
      session.getExtraInfo().getOwners().removeAll(sessionUpdateModel.getRemoveMemberSet());
    }
    if (sessionUpdateModel.getRemoveOwnerSet() != null) {
      sessionUpdateModel.getRemoveOwnerSet().remove(session.getExtraInfo().getCreatorId());
      owners.removeAll(sessionUpdateModel.getRemoveOwnerSet());
    }
    return session;
  }

  Message getMessage(String senderId, String sessionId, MessageType type);

  @Mapping(target = "data.sessionId", source = "sessionId")
  @Mapping(target = "type", source = "type")
  @Mapping(target = "id", ignore = true)
  SocketMessageModel<UpdateSessionModel> getUpdateMembersMessageModel(SocketMessageModel.Type type, String sessionId);

  @Mapping(target = "data", source = "session")
  @Mapping(target = "type", expression = "java(SocketMessageModel.Type.SESSION_EDITED)")
  @Mapping(target = "id", ignore = true)
  SocketMessageModel<UpdatedSessionModel> getSessionUpdatedMessage(Session session);

  @Mapping(ignore = true, target = "extraInfo.owners")
  @Mapping(target = "memberCount", expression = "java(session.getMembers() == null ? 0 : session.getMembers().size())")
  UpdatedSessionModel getSessionBodyModelFromSession(Session session);

}
