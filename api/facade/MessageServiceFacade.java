package org.tpl.chat.api.facade;


import lombok.RequiredArgsConstructor;
import org.tpl.chat.api.dto.EditMessageInputModel;
import org.tpl.chat.api.dto.MessageOutputModel;
import org.tpl.chat.api.dto.SeenOutputModel;
import org.tpl.chat.api.dto.StoryOutputModel;
import org.tpl.chat.api.facade.mapper.MessageFacadeMapper;
import org.tpl.chat.api.facade.mapper.StoryFacadeMapper;
import org.tpl.chat.dal.model.Message;
import org.tpl.chat.dal.model.MessageType;
import org.tpl.chat.dal.model.Story;
import org.tpl.chat.service.MessageService;
import org.tpl.chat.service.SessionService;
import org.tpl.chat.service.StoryService;
import org.tpl.chat.service.model.IterativePageState;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.tpl.util.common.service.exception.ValidationException;
import org.tpl.util.common.service.model.PageQueryParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.tpl.util.mongodbcommon.service.RepositoryUtils.getPageableFromPageQueryParams;

@Component
@RequiredArgsConstructor
public class MessageServiceFacade {
    private final MessageService messageService;
    private final SessionService sessionService;
    private final UserUtil userUtil;

    private final MessageFacadeMapper messageFacadeMapper;
    private final StoryFacadeMapper storyFacadeMapper;

    @Value("${max.voice.size}")
    private Long maxVoiceSize;
    @Value("${max.video.size}")
    private Long maxVideoSize;
    @Value("${max.image.size}")
    private Long maxImageSize;
    @Value("${max.file.size}")
    private Long maxFileSize;
    private final String voiceFormat = "audio";
    private final String videoFormat = "video";
    private final String imageFormat = "image";

    public MessageOutputModel send(String sessionId, MessageType type, String content, String repliedId, MultipartFile file, String requestId, String storyRepliedId) {
        validateTypeAndFile(type, file);
        var message = messageFacadeMapper.getEntity(userUtil.getUserId(), sessionId, type, repliedId, content, storyRepliedId);
        return messageFacadeMapper.getDtoFromMessageAndRequestId(messageService.send(message, file), requestId);
    }

    public Page<MessageOutputModel> getAll(String sessionId, PageQueryParams queryParams) {
        return messageService.getMessages(sessionId, getPageableFromPageQueryParams(queryParams)).map(messageFacadeMapper::getDto);
    }

    public List<MessageOutputModel> getAllMessagesIterative(String sessionId, String messageId, IterativePageState state, int limit) {
        return messageService.getMessagesIterative(sessionId, messageId, state, limit).stream().map(messageFacadeMapper::getDto).collect(Collectors.toList());
    }

    public MessageOutputModel editMessage(String messageId, EditMessageInputModel inputModel) {
        return messageFacadeMapper.getDto(messageService.editMessage(messageId, inputModel.getContent()));
    }

    public void deleteMessage(String messageId) {
        messageService.deleteById(messageId);
    }

    public SeenOutputModel seenMessage(String messageId) {
        var sessionId = messageService.seenMessage(messageId);
        var unreadMessageCount = messageService.getUnreadMessageCount(userUtil.getUserId(), sessionService.getById(sessionId));
        return new SeenOutputModel(unreadMessageCount);
    }

    public void sendReaction(String messageId, String reaction) {
        messageService.sendReaction(messageId, reaction, userUtil.getUserId());
    }


    public void pinMessage(String messageId) {
        messageService.pin(messageId);
    }

    public void unpinMessage(String messageId) {
        messageService.unpin(messageId);
    }

    public void sendTypingStatus(String sessionId) {
        messageService.sendTypingStatus(sessionId, userUtil.getUserId());
    }

    private void validateTypeAndFile(MessageType type, MultipartFile file) {
        if (MessageType.NEW_MEMBERS.equals(type) || MessageType.REMOVE_MEMBERS.equals(type) || MessageType.CREATED.equals(type)) {
            throw new ValidationException("type", LocaleConfig.getLocaleMessage("invalid.message.type"));
        }
        if (MessageType.TEXT.equals(type) && Objects.nonNull(file)) {
            throw new ValidationException("file", LocaleConfig.getLocaleMessage("general.null"));
        }
        if (!MessageType.TEXT.equals(type) && Objects.isNull(file)) {
            throw new ValidationException("file", LocaleConfig.getLocaleMessage("general.not.null"));
        }
        if (!MessageType.TEXT.equals(type) && file.getSize() > getMaxFileSize(type)) {
            throw new ValidationException("file", LocaleConfig.getLocaleMessage("file.size.is.too.large"));
        }
        if (!MessageType.TEXT.equals(type) && !MessageType.FILE.equals(type) && !file.getContentType().startsWith(getFileFormat(type))) {
            throw new ValidationException("file", LocaleConfig.getLocaleMessage("file.format.is.incorrect"));
        }
    }

    private Long getMaxFileSize(MessageType type) {
        if (MessageType.VOICE.equals(type)) return maxVoiceSize;
        if (MessageType.IMAGE.equals(type)) return maxImageSize;
        if (MessageType.VIDEO.equals(type)) return maxVideoSize;
        if (MessageType.FILE.equals(type)) return maxFileSize;
        throw new IllegalStateException("invalid type");
    }

    private String getFileFormat(MessageType type) {
        if (MessageType.VOICE.equals(type)) return voiceFormat;
        if (MessageType.IMAGE.equals(type)) return imageFormat;
        if (MessageType.VIDEO.equals(type)) return videoFormat;
        throw new IllegalStateException("invalid type");
    }


}
