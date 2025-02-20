package org.tpl.chat.service;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.MessageType;
import org.tpl.chat.dal.model.Story;
import org.tpl.chat.dal.model.StoryType;
import org.tpl.chat.dal.model.User;
import org.tpl.chat.dal.repository.StoryRepository;
import org.tpl.chat.service.mapper.StoryServiceMapper;
import org.tpl.chat.service.model.AccessPolicyModel;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.service.model.StoriesViewModel;
import org.tpl.chat.service.remote.FmAdapter;
import org.tpl.chat.service.remote.model.MultiPartFileUploadModel;
import org.tpl.chat.service.remote.usermanagement.model.RoleModel;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.tpl.util.common.service.exception.NotFoundException;
import org.tpl.util.common.service.exception.PreconditionFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private final StoryRepository repository;
    private final UserService userService;
    private final StoryServiceMapper mapper;
    private final UserUtil userUtil;
    private final FmAdapter fmAdapter;
    @Value("${story.expiration.hours}")
    private Integer expirationHours;
    @Value("${story.send.userLimitation}")
    private Integer userLimitation;

    @Override
    public Story send(Story story, MultipartFile file) {
        checkUserStoryLimitation();
        RoleModel role = userService.getCurrentRole();
        mapper.update(
                story,
                userUtil.getUserId(),
                role.getCode(),
                role.getMainOrganization().getCode(),
                role.getFormation().getCode()
        );
        uploadFileIfNeeded(story, file);
        story = repository.save(story);
        fillSenderOfStories(List.of(story));
        return story;
    }

    @Override
    public void seen(String storyId) {
        repository.seenStory(storyId, userUtil.getUserId());
    }

    @Override
    public Page<Story> getPage(String userId, Pageable pageable, Boolean activeStories) {
        Page<Story> stories;
        if (Objects.isNull(activeStories)){
            stories = getAllBySenderId(userId, pageable);
        }else if (activeStories){
            stories = getActiveStoriesBySenderId(userId, pageable);
        }else {
            stories = getNotActiveStoriesBySenderId(userId, pageable);
        }
        fillStoriesReceivers(stories.getContent());
        return stories;
    }

    @Override
    public Story getById(String storyId) {
        return repository.findById(storyId).orElseThrow(() -> new NotFoundException(LocaleConfig.getLocaleMessage("story.not.found")));
    }

    @Override
    public Optional<Story> getOptionalById(String storyId) {
        return repository.findById(storyId);
    }

    @Override
    public Page<MemberModel> getReceivers(String storyId, Pageable pageable) {
        Page<MemberModel> page = repository.getReceivers(storyId, pageable);
        fillMembersModelInfo(page.getContent());
        return page;
    }

    @Override
    public Page<StoriesViewModel> getStoriesBasedOnRole(Pageable pageable) {
        AccessPolicyModel accessPolicyModel = userService.getAccessPolicyModelByCurrentRole();
        LocalDateTime now = LocalDateTime.now();
        Page<StoriesViewModel> page = repository.getStoriesBasedOnRole(
                userUtil.getUserId(),
                pageable,
                accessPolicyModel,
                now.minusHours(expirationHours),
                now
        );
        fillSenderOfStoriesViewModel(page.getContent());
        return page;
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    private Page<Story> getAllBySenderId(String userId, Pageable pageable) {
        Page<Story> page = repository.findAllBySenderId(pageable, userId);
        fillSenderOfStories(page.getContent());
        return page;
    }

    private Page<Story> getActiveStoriesBySenderId(String userId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Story> page = repository.findAllBySenderIdAndCreatedDateBetween(pageable, userId, now.minusHours(expirationHours), now);
        fillSenderOfStories(page.getContent());
        return page;
    }

    private Page<Story> getNotActiveStoriesBySenderId(String userId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Story> page = repository.findAllBySenderIdAndCreatedDateBefore(pageable, userId, now.minusHours(expirationHours));
        fillSenderOfStories(page.getContent());
        return page;
    }

    private void uploadFileIfNeeded(Story story, MultipartFile file){
        if (file != null) {
            var fileUploadModel = fmAdapter.upload(MessageType.valueOf(story.getType().name()), new MultiPartFileUploadModel(file));
            story.setUrl(fileUploadModel.getUrl());
        }
    }

    private void checkUserStoryLimitation() {
        LocalDateTime now = LocalDateTime.now();
        long count = repository.countBySenderIdAndCreatedDateBetween(userUtil.getUserId(), now.minusHours(expirationHours), now);
        if (count < userLimitation) return;
        throw new PreconditionFailedException(LocaleConfig.getLocaleMessage("story.limitation.is.exited"));
    }

    private void fillSenderOfStories(List<Story> stories){
        if (CollectionUtils.isEmpty(stories)) return;
        Map<String, User> usersMap = null;
        try {
            usersMap = userService.getMapByIds(stories.stream().map(Story::getSenderId).collect(Collectors.toSet()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (usersMap != null) {
            for (Story story : stories) {
                User user = usersMap.get(story.getSenderId());
                if (user != null) {
                    story.setSender(new MemberModel(story.getSenderId(), user.getPersonnelPhotoUrl(), user.getFullName(), null));
                }
            }
        }
    }

    private void fillSenderOfStoriesViewModel(List<StoriesViewModel> stories){
        if (CollectionUtils.isEmpty(stories)) return;
        Map<String, User> usersMap = null;
        try {
            usersMap = userService.getMapByIds(stories.stream().map(StoriesViewModel::getSenderId).collect(Collectors.toSet()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (usersMap != null) {
            for (StoriesViewModel story : stories) {
                User user = usersMap.get(story.getSenderId());
                if (user != null) {
                    story.setSender(new MemberModel(story.getSenderId(), user.getPersonnelPhotoUrl(), user.getFullName(), null));
                }
            }
        }
    }

    private void fillMembersModelInfo(List<MemberModel> receivers) {
        if (CollectionUtils.isEmpty(receivers)) return;
        Map<String, User> usersMap = null;
        try {
            usersMap = userService.getMapByIds(receivers.stream().map(MemberModel::getId).collect(Collectors.toSet()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (usersMap != null) {
            for (MemberModel memberModel : receivers) {
                User user = usersMap.get(memberModel.getId());
                if (user != null) {
                    memberModel.setName(user.getFullName());
                    memberModel.setImageUrl(user.getPersonnelPhotoUrl());
                }
            }
        }
    }

    private void fillStoriesReceivers(List<Story> stories) {
        if (CollectionUtils.isEmpty(stories)) return;
        stories.forEach(story -> {
            story.setReceiverIds(repository.findById(story.getId()).get().getReceiverIds());
        });
    }
}
