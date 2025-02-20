package org.tpl.chat.service.annotation;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.tpl.chat.dal.model.Story;
import org.tpl.chat.service.StoryService;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor
public class StoryPermissionAspect {

    private final StoryService storyService;
    private final UserUtil userUtil;

    @Around("@annotation(StoryPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        var signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();
        var permission = method.getAnnotation(StoryPermission.class);
        boolean isSender = permission.sender();
        String storyId = Objects.nonNull(permission.storyId()) && !permission.storyId().isBlank() ?
                (String) CustomSpringExpressionLanguageParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), permission.storyId()) :
                null;
        if (storyId == null) {
            throw new IllegalStateException("at least one of these parameter must include. [storyId]");
        }
        Story story = storyService.getById(storyId);
        checkPermission(story, isSender);

        return joinPoint.proceed();
    }

    private void checkPermission(
            Story story,
            boolean isSender
    ) {
        var userId = userUtil.getUserId();
        if (isSender) isSender(story, userId);
    }


    private void isSender(Story story, String userId) {
        if (!story.getSenderId().equals(userId))
            throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
    }
}
