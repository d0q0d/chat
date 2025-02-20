package org.tpl.chat.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.tpl.chat.api.dto.StoriesViewOutputModel;
import org.tpl.chat.api.dto.StoryInputModel;
import org.tpl.chat.api.dto.StoryOutputModel;
import org.tpl.chat.api.facade.StoryFacade;
import org.tpl.chat.service.annotation.StoryPermission;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.util.common.service.model.PageQueryParams;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryFacade facade;

    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('b.81')")
    @Operation(summary = " ارسال استوری", description = "b.81")
    public StoryOutputModel send(@Validated @ModelAttribute StoryInputModel inputModel) {
        return facade.send(inputModel);
    }

    @PostMapping(value = "/seen/{storyId}")
    @PreAuthorize("hasAuthority('b.82')")
    @Operation(summary = "مشاهده استوری", description = "b.82")
    public void seen(@PathVariable("storyId") String storyId) {
        facade.seen(storyId);
    }

    @GetMapping(value = "/self")
    @PreAuthorize("hasAuthority('b.83')")
    @Operation(summary = "دریافت استوری های خود", description = "b.83")
    public Page<StoryOutputModel> getSelfStories(@Validated @ParameterObject PageQueryParams queryParams, @RequestParam(required = false) Boolean activeStories) {
        return facade.getSelfStories(queryParams, activeStories);
    }

    @GetMapping(value = "/receivers/{storyId}")
    @PreAuthorize("hasAuthority('b.84')")
    @Operation(summary = "دریافت کننده های استوری خود", description = "b.84")
    @StoryPermission(sender = true, storyId = "#storyId")
    public Page<MemberModel> getReceivers(@Validated @ParameterObject PageQueryParams queryParams, @PathVariable("storyId") String storyId) {
        return facade.getReceivers(queryParams, storyId);
    }

    @GetMapping(value = "/based-on-role")
    @PreAuthorize("hasAuthority('b.85')")
    @Operation(summary = "دریافت استوری ها بر اساس نقش", description = "b.85")
    public Page<StoriesViewOutputModel> getStoriesBasedOnRole(@Validated @ParameterObject PageQueryParams queryParams) {
        return facade.getStoriesBasedOnRole(queryParams);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('b.86')")
    @Operation(summary = "حذف استوری", description = "b.86")
    @StoryPermission(sender = true, storyId = "#id")
    public void deleteStory(@PathVariable("id") String id) {
        facade.delete(id);
    }

}
