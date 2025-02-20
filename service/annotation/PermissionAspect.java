package org.tpl.chat.service.annotation;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.tpl.chat.dal.model.Message;
import org.tpl.chat.dal.model.Session;
import org.tpl.chat.dal.model.SessionType;
import org.tpl.chat.service.MessageService;
import org.tpl.chat.service.SessionService;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.tpl.util.common.service.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final SessionService sessionService;
    private final MessageService messageService;
    private final UserUtil userUtil;

    @Around("@annotation(Permission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        var signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();
        var permission = method.getAnnotation(Permission.class);
        boolean isMember = permission.member();
        boolean isP2pMember = permission.p2pMember();
        boolean isGroupMember = permission.groupMember();
        boolean isGroupOwner = permission.groupOwner();
        boolean isGroupCreator = permission.groupCreator();
        boolean isChannelMember = permission.channelMember();
        boolean isChannelOwner = permission.channelOwner();
        boolean isChannelCreator = permission.channelCreator();
        boolean isSender = permission.sender();
        boolean ignoreNotExistsSession = permission.ignoreNotExistsSession();
        SessionType[] acceptableTypes = permission.acceptableTypes();
        String messageId = Objects.nonNull(permission.messageId()) && !permission.messageId().isBlank() ?
                (String) CustomSpringExpressionLanguageParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), permission.messageId()) :
                null;
        String sessionId = Objects.nonNull(permission.sessionId()) && !permission.sessionId().isBlank() ?
                (String) CustomSpringExpressionLanguageParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), permission.sessionId()) :
                null;
        if (messageId == null && sessionId == null) {
            throw new IllegalStateException("at least one of these parameter must include. [session-id, message-id]");
        }
        if (sessionId != null) {
            var session = sessionService.getById(sessionId);
            if (!ignoreNotExistsSession && session == null) throw new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found"));
            if (session != null){
                if (Arrays.stream(acceptableTypes).noneMatch(sessionType -> sessionType.equals(session.getSessionType()))) throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
                checkPermission(session, isMember, isP2pMember, isGroupMember, isGroupOwner, isGroupCreator, isChannelMember, isChannelOwner, isChannelCreator);
            }
        }
        if (messageId != null) {
            var message = messageService.getById(messageId);
            if (message == null) throw new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found"));
            if (isSender) isSender(message);
            var session = sessionService.getById(message.getSessionId());
            if (session == null) throw new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found"));
            if (Arrays.stream(acceptableTypes).noneMatch(sessionType -> sessionType.equals(session.getSessionType()))) throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
            checkPermission(session, isMember, isP2pMember, isGroupMember, isGroupOwner, isGroupCreator, isChannelMember, isChannelOwner, isChannelCreator);
        }
        return joinPoint.proceed();
    }

    private void checkPermission(
            Session session,
            boolean isMember,
            boolean isP2pMember,
            boolean isGroupMember,
            boolean isGroupOwner,
            boolean isGroupCreator,
            boolean isChannelMember,
            boolean isChannelOwner,
            boolean isChannelCreator
    ) {
        var userId = userUtil.getUserId();
        if (isMember) isMember(session, userId);
        if (SessionType.P2P.equals(session.getSessionType())) {
            if (isP2pMember) isMember(session, userId);
        }
        if (SessionType.GROUP.equals(session.getSessionType())) {
            if (isGroupMember) isMember(session, userId);
            if (isGroupOwner) isOwner(session, userId);
            if (isGroupCreator) isCreator(session, userId);
        }
        if (SessionType.CHANNEL.equals(session.getSessionType())) {
            if (isChannelMember) isMember(session, userId);
            if (isChannelOwner) isOwner(session, userId);
            if (isChannelCreator) isCreator(session, userId);
        }
    }

    private void isCreator(Session session, String userId) {
        if (Objects.isNull(session.getExtraInfo()) || !session.getExtraInfo().getCreatorId().equals(userId)) {
            throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
        }
    }

    private void isOwner(Session session, String userId) {
        if (Objects.isNull(session.getExtraInfo()) || Objects.isNull(session.getExtraInfo().getOwners()) || !session.getExtraInfo().getOwners().contains(userId)) {
            throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
        }
    }

    private void isMember(Session session, String userId) {
        if (!session.getMembers().contains(userId)) {
            throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
        }
    }

    private void isSender(Message message) {
        if (!message.getSenderId().equals(userUtil.getUserId()))
            throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
    }
}
