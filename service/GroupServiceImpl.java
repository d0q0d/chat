package org.tpl.chat.service;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.*;
import org.tpl.chat.service.mapper.GroupServiceMapper;
import org.tpl.chat.service.messagebroker.ProducerHandler;
import org.tpl.chat.service.model.SocketMessageModel;
import org.tpl.chat.service.remote.FmAdapter;
import org.tpl.chat.service.remote.model.MultiPartFileUploadModel;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.tpl.util.common.service.exception.NotFoundException;
import org.tpl.util.common.service.exception.PreconditionFailedException;
import org.tpl.util.common.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

  private final SessionService sessionService;
  private final GroupServiceMapper mapper;
  private final FmAdapter fmAdapter;
  private final UserUtil userUtil;
  private final UserService userService;
  private final MessageService messageService;
  private final ProducerHandler producerHandler;

    public Session create(Group group) {
        checkMembers(group.getMembers());
        uploadAndSetImageUrl(group);
      Session session = sessionService.save(mapper.groupToSessionWithAdditionalInfo(group));
      sendMessagesForAddOrRemoveMembers(session.getId(), session.getMembers(), MessageType.NEW_MEMBERS, SocketMessageModel.Type.ADDED_TO_MEMBERS);
      sendCreatedMessage(session.getId());
      return sessionService.getById(session.getId());
    }

  @Override
  public Session partialUpdate(SessionUpdateModel inputModel) {
    var session = sessionService.getById(inputModel.getSessionId());
    if (session == null)
      throw new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found"));
    checkSessionType(session);
    checkForAddingOwners(session, inputModel.getAddOwnerSet());
    checkMembers(inputModel.getAddMemberSet());
    mapper.updateSession(session, inputModel);
    session = sessionService.save(session);
    sendMessagesForAddOrRemoveMembers(session.getId(), inputModel.getAddMemberSet(), MessageType.NEW_MEMBERS, SocketMessageModel.Type.ADDED_TO_MEMBERS);
    sendMessagesForAddOrRemoveMembers(session.getId(), inputModel.getRemoveMemberSet(), MessageType.REMOVE_MEMBERS, SocketMessageModel.Type.REMOVED_FROM_MEMBERS);
    sendMessageOnSocketForUpdatingOwners(SocketMessageModel.Type.ADDED_TO_OWNERS, session.getId(), inputModel.getAddOwnerSet());
    sendMessageOnSocketForUpdatingOwners(SocketMessageModel.Type.REMOVED_FROM_OWNERS, session.getId(), inputModel.getRemoveOwnerSet());
    session = sessionService.getById(session.getId());
    sendSessionUpdatedMessageOnSocket(session);
    return session;
  }

  @Override
  public Session updateGroupImage(MultipartFile file, String sessionId) {
    var session = sessionService.getById(sessionId);
    if (session == null)
      throw new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found"));
    checkSessionType(session);
    session.setImageUrl(uploadImage(file));
    session = sessionService.save(session);
    session = sessionService.getById(session.getId());
    sendSessionUpdatedMessageOnSocket(session);
    return session;
  }

  private void sendSessionUpdatedMessageOnSocket(Session session) {
    if (CollectionUtils.isEmpty(session.getMembers())) return;
    session.getMembers().parallelStream().forEach(member -> {
      if (!member.equals(userUtil.getUserId())){
        producerHandler.sendAndSaveInMemory(member, mapper.getSessionUpdatedMessage(session));
      }
    });
  }

  private void uploadAndSetImageUrl(Group group) {
    if (group.getFile() != null) {
      var imageUrl = uploadImage(group.getFile());
      group.setImageUrl(imageUrl);
    }
  }

  private String uploadImage(MultipartFile file) {
    return fmAdapter.upload(MessageType.IMAGE, new MultiPartFileUploadModel(file)).getUrl();
  }

  private void checkMembers(Set<String> members) {
    if (CollectionUtils.isEmpty(members)) return;
    Set<String> responseIds =
        userService.getByIds(members).stream()
            .map(User::getUserId)
            .collect(Collectors.toSet());
    if (members.size() != responseIds.size())
      throw new ValidationException(LocaleConfig.getLocaleMessage("members.are.not.valid"));
  }

  private void checkForAddingOwners(Session session, Set<String> addOwnerSet) {
    if (addOwnerSet == null) return;
    for (String userId : addOwnerSet) {
      if (!session.getMembers().contains(userId))
        throw new PreconditionFailedException(LocaleConfig.getLocaleMessage("user.not.exist.in.session"));
    }
  }

  private void checkSessionType(Session session){
    if(!SessionType.GROUP.equals(session.getSessionType()))
      throw new ValidationException(LocaleConfig.getLocaleMessage("invalid.session"));
  }

  private void sendMessagesForAddOrRemoveMembers(String sessionId, Set<String> members, MessageType type, SocketMessageModel.Type socketMessageType) {
    if (CollectionUtils.isEmpty(members)) return;
    Map<String, User> membersName = userService.getMapByIds(members);
    members.forEach(member -> {
      producerHandler.sendAndSaveInMemory(member, mapper.getUpdateMembersMessageModel(socketMessageType, sessionId));
      Message message = mapper.getMessage(userUtil.getUserId(), sessionId, type, membersName.get(member));
      messageService.send(message, null);
    });
  }

  private void sendCreatedMessage(String sessionId) {
    Message message = mapper.getCreatedMessage(userUtil.getUserId(), sessionId, MessageType.CREATED);
    messageService.send(message, null);
  }

  private void sendMessageOnSocketForUpdatingOwners(SocketMessageModel.Type type, String sessionId, Set<String> set) {
    if (CollectionUtils.isEmpty(set)) return;
    set.parallelStream().forEach(member -> {
      producerHandler.sendAndSaveInMemory(member, mapper.getUpdateMembersMessageModel(type, sessionId));
    });
  }

}
