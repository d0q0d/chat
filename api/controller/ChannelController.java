package org.tpl.chat.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.tpl.chat.api.dto.CreateChannelInputModel;
import org.tpl.chat.api.dto.SessionOutputModel;
import org.tpl.chat.api.dto.UpdateChannelInfoInputModel;
import org.tpl.chat.api.dto.UpdateImageInputModel;
import org.tpl.chat.api.facade.ChannelServiceFacade;
import org.tpl.chat.service.annotation.Permission;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelServiceFacade channelServiceFacade;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('b.1')")
    @Operation(summary = "ایجاد کانال", description = "b.1")
    public SessionOutputModel create(@Validated @ModelAttribute CreateChannelInputModel inputModel) {
        return channelServiceFacade.create(inputModel);
    }

    @PatchMapping(path = "/{session-id}")
    @PreAuthorize("hasAuthority('b.2')")
    @Operation(summary = "آپدیت اطلاعات کانال", description = "b.2")
    @Permission(channelOwner = true, sessionId = "#sessionId")
    public SessionOutputModel update(@Validated @RequestBody UpdateChannelInfoInputModel inputModel, @PathVariable("session-id") String sessionId) {
        return channelServiceFacade.updateChannelInfo(inputModel, sessionId);
    }

    @PutMapping(path = "/image/{session-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('b.3')")
    @Operation(summary = "آپدیت تصویر کانال", description = "b.3")
    @Permission(channelOwner = true, sessionId = "#sessionId")
    public SessionOutputModel updateChannelImage(@Validated @ModelAttribute UpdateImageInputModel inputModel, @PathVariable("session-id") String sessionId) {
        return channelServiceFacade.updateChannelImage(inputModel, sessionId);
    }

}
