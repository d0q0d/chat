package org.tpl.chat.api.facade.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tpl.chat.api.dto.StoriesViewOutputModel;
import org.tpl.chat.api.dto.StoryInputModel;
import org.tpl.chat.api.dto.StoryOutputModel;
import org.tpl.chat.dal.model.Story;
import org.tpl.chat.service.model.StoriesViewModel;

@Mapper(componentModel = "spring")
public interface StoryFacadeMapper {

    Story getStoryFromInputModel(StoryInputModel inputModel);

    @Mapping(target = "receiversCount", expression = "java(story.getReceiverIds() == null ? 0 : story.getReceiverIds().size())")
    StoryOutputModel getStoryOutputModelFromStory(Story story);

    StoriesViewOutputModel getStoriesViewOutputModelFromModel(StoriesViewModel storiesViewModel);

}
