package org.tpl.chat.api.facade;


import lombok.RequiredArgsConstructor;
import org.tpl.chat.api.dto.CreateChannelInputModel;
import org.tpl.chat.api.dto.SessionOutputModel;
import org.tpl.chat.api.dto.UpdateChannelInfoInputModel;
import org.tpl.chat.api.dto.UpdateImageInputModel;
import org.tpl.chat.api.facade.mapper.ChannelFacadeMapper;
import org.tpl.chat.api.facade.mapper.SessionFacadeMapper;
import org.tpl.chat.dal.model.Session;
import org.tpl.chat.service.ChannelService;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.tpl.util.common.service.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ChannelServiceFacade {
    private final ChannelService channelService;
    private final UserUtil userUtil;
    private final ChannelFacadeMapper mapper;
    private final SessionFacadeMapper sessionFacadeMapper;
    @Value("${max.channel.group.image.size}")
    private Long maxChannelGroupImageSize;
    private final String imageFormat = "image";

    public SessionOutputModel create(CreateChannelInputModel inputModel) {
        validateImage(inputModel.getFile());
        Session session = channelService.create(mapper.createChannelInputModelToChannel(inputModel, userUtil.getUserId()));
        return sessionFacadeMapper.sessionToSessionOutputModel(session);
    }

    public SessionOutputModel updateChannelInfo(UpdateChannelInfoInputModel inputModel, String sessionId) {
        Session session = channelService.partialUpdate(mapper.updateChannelInputModelToChannel(inputModel, sessionId));
        return sessionFacadeMapper.sessionToSessionOutputModel(session);
    }

    public SessionOutputModel updateChannelImage(UpdateImageInputModel inputModel, String sessionId) {
        validateImage(inputModel.getFile());
        Session session = channelService.updateChannelImage(inputModel.getFile(), sessionId);
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