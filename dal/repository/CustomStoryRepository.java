package org.tpl.chat.dal.repository;

import org.tpl.chat.dal.model.Story;
import org.tpl.chat.service.model.AccessPolicyModel;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.service.model.StoriesViewModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface CustomStoryRepository {
    void seenStory(String storyId, String userId);

    Page<MemberModel> getReceivers(String storyId, Pageable pageable);

    Page<StoriesViewModel> getStoriesBasedOnRole(String userId, Pageable pageable, AccessPolicyModel accessPolicyModel, LocalDateTime from, LocalDateTime to);

}
