package org.tpl.chat.api.facade;


import org.tpl.chat.api.dto.SessionOutputModel;
import org.tpl.chat.api.dto.UserInfoOutputModel;
import org.tpl.chat.api.facade.mapper.SessionFacadeMapper;
import org.tpl.chat.dal.model.Session;
import org.tpl.chat.service.SessionService;
import org.tpl.chat.service.UserInfoService;
import org.tpl.chat.service.UserService;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.tpl.util.common.service.exception.NotFoundException;
import org.tpl.util.common.service.model.PageQueryParams;
import org.tpl.util.mongodbcommon.service.RepositoryUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public record SessionServiceFacade(SessionService sessionService, UserInfoService userInfoService, UserUtil userUtil,
                                   SessionFacadeMapper mapper) {

    public List<SessionOutputModel> getAll() {
        return sessionService
                .getAll(userUtil.getUserId())
                .stream()
                .map(mapper::sessionToSessionOutputModel)
                .collect(Collectors.toList());
    }

    public SessionOutputModel get(String sessionId) {
        Session session = sessionService.getById(sessionId);
        if(session == null) throw new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found"));
        return mapper.sessionToSessionOutputModel(session);
    }

    public void delete(String sessionId) {
        sessionService.deleteById(sessionId, userUtil.getUserId());
    }

    public Page<MemberModel> getMembers(String sessionId, PageQueryParams queryParams) {
        return sessionService.getMembers(sessionId, RepositoryUtils.getPageableFromPageQueryParams(queryParams));
    }

    public void pinSession(String sessionId) {
        userInfoService.pinSession(userUtil.getUserId(), sessionId);
    }

    public void unpinSession(String sessionId) {
        userInfoService.unpinSession(userUtil.getUserId(), sessionId);
    }
}
