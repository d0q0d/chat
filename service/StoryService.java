package org.tpl.chat.service;

import org.tpl.chat.dal.model.Story;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.service.model.StoriesViewModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface StoryService {
    Story send(Story story, MultipartFile file);

    void seen(String storyId);

    Page<Story> getPage(String userId, Pageable pageable, Boolean activeStories);

    Story getById(String storyId);

    Optional<Story> getOptionalById(String storyId);

    Page<MemberModel> getReceivers(String storyId, Pageable pageable);

    Page<StoriesViewModel> getStoriesBasedOnRole(Pageable pageable);

    void delete(String id);
}
