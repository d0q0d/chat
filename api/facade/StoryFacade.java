package org.tpl.chat.api.facade;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.api.dto.StoriesViewOutputModel;
import org.tpl.chat.api.dto.StoryInputModel;
import org.tpl.chat.api.dto.StoryOutputModel;
import org.tpl.chat.api.facade.mapper.StoryFacadeMapper;
import org.tpl.chat.dal.model.Story;
import org.tpl.chat.dal.model.StoryType;
import org.tpl.chat.service.StoryService;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.service.model.StoriesViewModel;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.tpl.util.common.service.exception.ValidationException;
import org.tpl.util.common.service.model.PageQueryParams;
import org.tpl.util.mongodbcommon.service.RepositoryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StoryFacade {

    private final StoryFacadeMapper mapper;
    private final StoryService storyService;
    private final UserUtil userUtil;
    @Value("${max.voice.size}")
    private Long maxVoiceSize;
    @Value("${max.video.size}")
    private Long maxVideoSize;
    @Value("${max.image.size}")
    private Long maxImageSize;
    private final String voiceFormat = "audio";
    private final String videoFormat = "video";
    private final String imageFormat = "image";

    public StoryOutputModel send(StoryInputModel inputModel) {
        validateTypeAndFile(inputModel.getType(), inputModel.getFile());
        Story story = mapper.getStoryFromInputModel(inputModel);
        story = storyService.send(story, inputModel.getFile());
        return mapper.getStoryOutputModelFromStory(story);
    }

    public void seen(String storyId) {
        storyService.seen(storyId);
    }

    public Page<StoryOutputModel> getSelfStories(PageQueryParams queryParams, Boolean activeStories) {
        Page<Story> page = storyService.getPage(userUtil.getUserId(), RepositoryUtils.getPageableFromPageQueryParams(queryParams), activeStories);
        return page.map(mapper::getStoryOutputModelFromStory);
    }

    public Page<MemberModel> getReceivers(PageQueryParams queryParams, String storyId) {
        return storyService.getReceivers(storyId, RepositoryUtils.getPageableFromPageQueryParams(queryParams));
    }

    public Page<StoriesViewOutputModel> getStoriesBasedOnRole(PageQueryParams queryParams) {
        Page<StoriesViewModel> page = storyService.getStoriesBasedOnRole(RepositoryUtils.getPageableFromPageQueryParams(queryParams));
        return page.map(mapper::getStoriesViewOutputModelFromModel);
    }

    public void delete(String id) {
        storyService.delete(id);
    }

    private void validateTypeAndFile(StoryType type, MultipartFile file) {
        if (StoryType.TEXT.equals(type) && Objects.nonNull(file)) {
            throw new ValidationException("file", LocaleConfig.getLocaleMessage("general.null"));
        }
        if (!StoryType.TEXT.equals(type) && Objects.isNull(file)) {
            throw new ValidationException("file", LocaleConfig.getLocaleMessage("general.not.null"));
        }
        if (!StoryType.TEXT.equals(type) && file.getSize() > getMaxFileSize(type)) {
            throw new ValidationException("file", LocaleConfig.getLocaleMessage("file.size.is.too.large"));
        }
        if (!StoryType.TEXT.equals(type) && !file.getContentType().startsWith(getFileFormat(type))) {
            throw new ValidationException("file", LocaleConfig.getLocaleMessage("file.format.is.incorrect"));
        }
    }

    private Long getMaxFileSize(StoryType type) {
        if (StoryType.VOICE.equals(type)) return maxVoiceSize;
        if (StoryType.IMAGE.equals(type)) return maxImageSize;
        if (StoryType.VIDEO.equals(type)) return maxVideoSize;
        throw new IllegalStateException("invalid type");
    }

    private String getFileFormat(StoryType type) {
        if (StoryType.VOICE.equals(type)) return voiceFormat;
        if (StoryType.IMAGE.equals(type)) return imageFormat;
        if (StoryType.VIDEO.equals(type)) return videoFormat;
        throw new IllegalStateException("invalid type");
    }

}
