package org.tpl.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tpl.chat.dal.model.*;
import org.tpl.chat.dal.repository.SessionRepository;
import org.tpl.chat.service.mapper.SessionServiceMapper;
import org.tpl.chat.service.messagebroker.ProducerHandler;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.service.model.MessagePreviewModel;
import org.tpl.chat.service.model.SessionRole;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.tpl.util.common.service.exception.NotFoundException;
import org.tpl.util.common.service.exception.ValidationException;
import org.tpl.util.mongodbcommon.dal.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.tpl.chat.dal.model.SessionType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final SessionRepository repository;
    private final SessionServiceMapper mapper;
    private final UserService userService;
    private final MessageService messageService;
    private final UserUtil userUtil;
    private final ProducerHandler producerHandler;
    private final UserInfoService userInfoService;

    @Override
    public Session getById(String id) {
        var session = repository.getById(id);
        if (session != null) {
            fillExtraInfoOfSessionList(userUtil.getUserId(), List.of(session));
        }
        return session;
    }

    @Override
    public Session save(Session session) {
        return repository.save(session);
    }

    @Override
    public Session getByMembersAndType(Set<String> members, SessionType sessionType) {
        return repository.getByMembersAndSizeAndSessionType(members, members.size(), sessionType);
    }

    @Override
    public List<Session> getAll(String userId) {
        List<Session> sessions = repository.findAllByMembers(userId);
        fillExtraInfoOfSessionList(userId, sessions);
        return sessions.stream()
                .sorted(
                        Comparator
                                .comparing(Session::getPinedDateTime, Comparator.nullsLast(Comparator.reverseOrder())
                                ).thenComparing(
                                        (Session session) ->
                                                session.getMessagePreview() == null
                                                        ? null
                                                        : session.getMessagePreview().getCreatedDate(),
                                        Comparator.nullsLast(Comparator.reverseOrder())
                                )
                )
                .collect(Collectors.toList());
    }

    @Override
    public Page<MemberModel> getMembers(String sessionId, Pageable pageable) {
        Session session = repository.findByIdAndExcludeMembers(sessionId).orElseThrow(() -> new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found")));
        checkSessionType(session, Set.of(CHANNEL, GROUP));
        Page<MemberModel> page = repository.getMembers(sessionId, pageable);
        fillMembersModelInfo(session, page.getContent());
        return page;
    }

    @Override
    public void deleteById(String sessionId, String userId) {
        var session = repository.getById(sessionId);
        if (session == null) return;
        if (session.getSessionType().equals(P2P)) {
            deleteP2P(userId, session);
        } else deleteChannelOrGroup(session, userId);
    }

    @Override
    public SessionRole getRoleFromSessionAndUserId(Session session, String userId) {
        if (session.getExtraInfo().getCreatorId().equals(userId)) return SessionRole.CREATOR;
        else if (session.getExtraInfo().getOwners().contains(userId)) return SessionRole.ADMIN;
        return SessionRole.MEMBER;
    }

    @Override
    public MessagePreviewModel getMessagePreview(Session session) {
        Message message = messageService.getLastMessage(session.getId());
        return mapper.getMessagePreviewModelFromMessage(message);
    }

    @Override
    public GroupAndChannelCountOutputModel getGroupAndChannelCount(String userId) {
        var channelCount = repository.countBySessionTypeAndMembersIn(CHANNEL, userId);
        var groupCount =  repository.countBySessionTypeAndMembersIn(GROUP, userId);
        return new GroupAndChannelCountOutputModel(channelCount, groupCount);
    }

    @Override
    public GroupAndChannelCountOutputModel getGroupAndChannelCountCreatedByUser(String userId, LocalDateTime from, LocalDateTime to) {
        var channelCount = repository.countBySessionTypeAndCreatedByAndCreatedDateBetween(CHANNEL, userId, from, to);
        var groupCount =  repository.countBySessionTypeAndCreatedByAndCreatedDateBetween(GROUP, userId, from, to);
        return new GroupAndChannelCountOutputModel(channelCount, groupCount);
    }

    @Override
    public int getP2PCountCreatedByUser(String userId, LocalDateTime from, LocalDateTime to) {
        return repository.countBySessionTypeAndCreatedByAndCreatedDateBetween(P2P, userId, from, to);
    }

    private void checkSessionType(Session session, Set<SessionType> validTypes) {
        if (!validTypes.contains(session.getSessionType()))
            throw new ValidationException(LocaleConfig.getLocaleMessage("invalid.session"));
    }

    private Session fillMembers(Session session) {
        session.setMembers(repository.findById(session.getId()).map(Session::getMembers).orElse(null));
        return session;
    }

    private void deleteChannelOrGroup(Session session, String userId) {
        var extraInfo = session.getExtraInfo();
        if (extraInfo.getCreatorId().equals(userId)) {
            repository.deleteById(session.getId());
            sendDeletedSessionOnSocket(session.getId(), session.getMembers());
        } else {
            extraInfo.getOwners().remove(userId);
            session.getMembers().remove(userId);
            repository.save(session);
        }
    }

    private void sendDeletedSessionOnSocket(String sessionId, Set<String> members) {
        if (CollectionUtils.isEmpty(members)) return;
        members.remove(userUtil.getUserId());
        members.parallelStream().forEach(member -> {
            producerHandler.sendAndSaveInMemory(member, mapper.getSessionDeletedModel(sessionId));
        });
    }

    private void deleteP2P(String userId, Session session) {
        if (session.getSessionDeleteModelList() == null) {
            session.setSessionDeleteModelList(
                    List.of(new SessionDeleteModel(userId, LocalDateTime.now(), true)));
        } else {
            long count =
                    session.getSessionDeleteModelList().stream()
                            .filter(sessionDeleteModel -> sessionDeleteModel.getUserId().equals(userId))
                            .map(
                                    sessionDeleteModel -> {
                                        sessionDeleteModel.setIsDeleted(true);
                                        sessionDeleteModel.setLastDeletedDate(LocalDateTime.now());
                                        return sessionDeleteModel;
                                    })
                            .count();
            if (count == 0)
                session
                        .getSessionDeleteModelList()
                        .add(new SessionDeleteModel(userId, LocalDateTime.now(), true));
        }
        repository.save(session);
    }

    private void setUserInfoForP2PSessions(String userId, List<Session> sessions) {
        if (CollectionUtils.isEmpty(sessions)) return;
        Map<String, User> usersMap = null;
        try {
            usersMap = userService.getMapByIds(
                    sessions.stream()
                            .map(session -> {
                                if (Boolean.TRUE.equals(session.getIsSavedMessages())) return userId;
                                return session.getMembers().stream().filter(uId -> !uId.equals(userId)).toList().get(0);
                            })
                            .collect(Collectors.toSet())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (usersMap != null) {
            for (Session session : sessions) {
                User user = Boolean.TRUE.equals(session.getIsSavedMessages()) ?
                        usersMap.get(userId) :
                        usersMap.get(session.getMembers().stream().filter(uId -> !uId.equals(userId)).toList().get(0));
                if (user != null) mapper.setInfoForP2P(session, user);
            }
        }
    }

    private void setUnreadMessageCount(String userId, Session session) {
        session.setUnreadMessageCount(messageService.getUnreadMessageCount(userId, session));
    }

    private void setSecondSideUserIdForP2P(Session session, String userId) {
        if (!P2P.equals(session.getSessionType())) return;
        if (CollectionUtils.isEmpty(session.getMembers())) fillMembers(session);
        if (Boolean.TRUE.equals(session.getIsSavedMessages())) {
            session.setSecondSideUserId(userId);
        } else {
            session.setSecondSideUserId(session.getMembers().stream().filter(id -> !id.equals(userId)).findFirst().orElse(null));
        }
    }

    private void fillExtraInfoOfSessionList(String userId, List<Session> sessions) {
        List<Session> sessionNeedForFillUserInfo = new ArrayList<>();
        sessions.forEach(session -> {
            if (session.getSessionType().equals(P2P)) {
                sessionNeedForFillUserInfo.add(session);
            } else {
                session.setRole(getRoleFromSessionAndUserId(session, userId));
            }
            setSecondSideUserIdForP2P(session, userId);
            setUnreadMessageCount(userId, session);
            session.setMessagePreview(getMessagePreview(session));
            setOnlineStatusForP2P(userId, session);
            setPinedMessage(session);
            setPinedSessions(userId, session);
            setMembersCountAndOnlineMembersCount(session);
            setFirstUnreadMessageId(userId, session);
        });
        setUserInfoForP2PSessions(userId, sessionNeedForFillUserInfo);
    }

    private void setFirstUnreadMessageId(String userId, Session session) {
        session.setFirstUnreadMessageId(messageService.getFirstUnreadMessage(session, userId).map(BaseEntity::getId).orElse(null));
    }

    private void setMembersCountAndOnlineMembersCount(Session session) {
        session.setMemberCount(repository.getMembersCount(session.getId()));
        session.setOnlineMemberCount(repository.getOnlineMembersCount(session.getId()));
    }

    private void setPinedSessions(String userId, Session session) {
        var userInfo = userInfoService.getByUserId(userId);
        if (userInfo == null || userInfo.getPinedSessionsSet() == null) return;
        var pinedSessionOptional = userInfo.getPinedSessionsSet().stream().filter(pinedSession -> pinedSession.getSessionId().equals(session.getId())).findFirst();
        pinedSessionOptional.ifPresent(pinedSession -> session.setPinedDateTime(pinedSession.getPinedDateTime()));
    }

    private void setOnlineStatusForP2P(String userId, Session session) {
        var userInfo = userInfoService.getByUserId(session.getSecondSideUserId());
        if (userInfo != null) {
            session.setSecondSideLastSeen(userInfo.getLastSeen());
            session.setSecondSideOnlineStatus(userInfo.getOnlineStatus());
        }
    }

    private void setOnlineStatusForChannelAndGroup(MemberModel memberModel) {
        var userInfo = userInfoService.getByUserId(memberModel.getId());
        if (userInfo != null) {
            memberModel.setLastSeen(userInfo.getLastSeen());
            memberModel.setOnlineStatus(userInfo.getOnlineStatus());
        }
    }

    private void setPinedMessage(Session session) {
        if (session.getPinedMessageId() != null) {
            messageService.getOptionalById(session.getPinedMessageId()).ifPresent(session::setPinedMessage);
        }
    }

    private void fillMembersModelInfo(Session session, List<MemberModel> members) {
        if (CollectionUtils.isEmpty(members)) return;
        Map<String, User> usersMap = null;
        try {
            usersMap = userService.getMapByIds(members.stream().map(MemberModel::getId).collect(Collectors.toSet()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (usersMap != null) {
            for (MemberModel memberModel : members) {
                User user = usersMap.get(memberModel.getId());
                if (user != null) {
                    memberModel.setName(user.getFullName());
                    memberModel.setImageUrl(user.getPersonnelPhotoUrl());
                }
                memberModel.setRole(getRoleFromSessionAndUserId(session, memberModel.getId()));
            }
        }
    }

}
