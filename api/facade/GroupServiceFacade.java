package org.tpl.chat.api.facade;


import lombok.RequiredArgsConstructor;
import org.tpl.chat.api.dto.CreateGroupInputModel;
import org.tpl.chat.api.dto.SessionOutputModel;
import org.tpl.chat.api.dto.UpdateGroupInputModel;
import org.tpl.chat.api.dto.UpdateImageInputModel;
import org.tpl.chat.api.facade.mapper.GroupFacadeMapper;
import org.tpl.chat.api.facade.mapper.SessionFacadeMapper;
import org.tpl.chat.dal.model.Session;
import org.tpl.chat.service.GroupServiceImpl;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.tpl.util.common.service.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GroupServiceFacade {
    private final GroupServiceImpl groupService;
    private final UserUtil userUtil;
    private final GroupFacadeMapper mapper;
    private final SessionFacadeMapper sessionFacadeMapper;
    @Value("${max.channel.group.image.size}")
    private Long maxChannelGroupImageSize;
    private final String imageFormat = "image";

    public SessionOutputModel create(CreateGroupInputModel inputModel) {
        validateImage(inputModel.getFile());
        Session session = groupService.create(mapper.getGroupModel(inputModel.getName(), inputModel.getMembers(), inputModel.getFile(), userUtil.getUserId()));
        return sessionFacadeMapper.sessionToSessionOutputModel(session);
    }

    public SessionOutputModel update(UpdateGroupInputModel inputModel, String sessionId) {
        Session session = groupService.partialUpdate(mapper.updateGroupInputModelToSessionUpdateModel(inputModel, sessionId));
        return sessionFacadeMapper.sessionToSessionOutputModel(session);
    }

    public SessionOutputModel updateGroupImage(UpdateImageInputModel inputModel, String sessionId) {
        validateImage(inputModel.getFile());
        Session session = groupService.updateGroupImage(inputModel.getFile(), sessionId);
        return sessionFacadeMapper.sessionToSessionOutputModel(session);
    }

    private void validateImage(MultipartFile file) {
        if (Objects.nonNull(file) && file.getSize() > maxChannelGroupImageSize) {
            throw new ValidationException(LocaleConfig.getLocaleMessage("file.size.is.too.large"));
        }
        if (Objects.nonNull(file) && !file.getContentType().startsWith(imageFormat)) {
            throw new ValidationException(LocaleConfig.getLocaleMessage("file.format.is.incorrect"));
        }
    }

}