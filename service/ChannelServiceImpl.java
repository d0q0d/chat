package org.tpl.chat.service;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.*;
import org.tpl.chat.service.mapper.ChannelServiceMapper;
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

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final SessionService sessionService;
    private final ChannelServiceMapper mapper;
    private final UserUtil userUtil;
    private final FmAdapter fmAdapter;
    private final UserService userService;
    private final MessageService messageService;
    private final ProducerHandler producerHandler;

    @Override
    public Session create(Channel channel) {
        checkMembers(channel.getMembers());
        uploadAndSetImageUrl(channel);
        Session session = sessionService.save(mapper.channelToSessionWithAdditionalInfo(channel));
        sendMessagesForAddOrRemoveMembers(session.getId(), session.getMembers(), SocketMessageModel.Type.ADDED_TO_MEMBERS);
        sendCreatedMessage(session.getId());
        return sessionService.getById(session.getId());
    }

    @Override
    public Session partialUpdate(SessionUpdateModel sessionUpdateModel) {
        var session = sessionService.getById(sessionUpdateModel.getSessionId());
        if (session == null)
            throw new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found"));
        checkSessionType(session);
        checkForAddingOwners(session, sessionUpdateModel.getAddOwnerSet());
        checkMembers(sessionUpdateModel.getAddMemberSet());
        mapper.updateSession(session, sessionUpdateModel);
        session = sessionService.save(session);
        sendMessagesForAddOrRemoveMembers(session.getId(), sessionUpdateModel.getAddMemberSet(), SocketMessageModel.Type.ADDED_TO_MEMBERS);
        sendMessagesForAddOrRemoveMembers(session.getId(), sessionUpdateModel.getRemoveMemberSet(), SocketMessageModel.Type.REMOVED_FROM_MEMBERS);
        sendMessageOnSocketForUpdatingOwners(SocketMessageModel.Type.ADDED_TO_OWNERS, session.getId(), sessionUpdateModel.getAddOwnerSet());
        sendMessageOnSocketForUpdatingOwners(SocketMessageModel.Type.REMOVED_FROM_OWNERS, session.getId(), sessionUpdateModel.getRemoveOwnerSet());
        session = sessionService.getById(session.getId());
        sendSessionUpdatedMessageOnSocket(session);
        return session;
    }

    @Override
    public Session updateChannelImage(MultipartFile file, String sessionId) {
        var session = sessionService.getById(sessionId);
        if (session == null)
            throw new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found"));
        checkSessionType(session);
        if (file != null) session.setImageUrl(uploadImage(file));
        else session.setImageUrl(null);
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

    private void sendMessageOnSocketForUpdatingOwners(SocketMessageModel.Type type, String sessionId, Set<String> set) {
        if (CollectionUtils.isEmpty(set)) return;
        set.parallelStream().forEach(member -> {
            producerHandler.sendAndSaveInMemory(member, mapper.getUpdateMembersMessageModel(type, sessionId));
        });
    }

    private void uploadAndSetImageUrl(Channel channel) {
        if (channel.getFile() != null) {
            var imageUrl = uploadImage(channel.getFile());
            channel.setImageUrl(imageUrl);
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

    private void checkSessionType(Session session) {
        if (!SessionType.CHANNEL.equals(session.getSessionType()))
            throw new ValidationException(LocaleConfig.getLocaleMessage("invalid.session"));
    }

    private void sendCreatedMessage(String sessionId) {
        Message message = mapper.getMessage(userUtil.getUserId(), sessionId, MessageType.CREATED);
        messageService.send(message, null);
    }

    private void sendMessagesForAddOrRemoveMembers(String sessionId, Set<String> members, SocketMessageModel.Type socketMessageType) {
        if (CollectionUtils.isEmpty(members)) return;
        members.forEach(member -> {
            producerHandler.sendAndSaveInMemory(member, mapper.getUpdateMembersMessageModel(socketMessageType, sessionId));
        });
    }

}
