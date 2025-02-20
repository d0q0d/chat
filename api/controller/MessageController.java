package org.tpl.chat.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.tpl.chat.api.dto.EditMessageInputModel;
import org.tpl.chat.api.dto.MessageOutputModel;
import org.tpl.chat.api.dto.ReactionInputModel;
import org.tpl.chat.api.dto.SeenOutputModel;
import org.tpl.chat.api.facade.MessageServiceFacade;
import org.tpl.chat.dal.model.MessageType;
import org.tpl.chat.service.annotation.Permission;
import org.tpl.chat.service.model.IterativePageState;
import org.tpl.util.common.service.model.PageQueryParams;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.tpl.chat.dal.model.SessionType.CHANNEL;
import static org.tpl.chat.dal.model.SessionType.GROUP;

@RestController
@RequestMapping("/api/v1/chat/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageServiceFacade messageServiceFacade;

    @PostMapping(value = "/send/{session-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('b.21')")
    @Operation(summary = " ارسال پیام", description = "b.21")
    @Permission(p2pMember = true, groupMember = true, channelOwner = true, ignoreNotExistsSession = true, sessionId = "#sessionId")
    public MessageOutputModel send(
            @PathVariable("session-id") String sessionId,
            @RequestPart @Schema(example = "TEXT", description = "TEXT VOICE VIDEO IMAGE FILE") String type,
            @RequestPart(required = false) String text,
            @RequestPart(required = false) String repliedId,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) String requestId,
            @RequestPart(required = false) String storyId
    ) {
        return messageServiceFacade.send(sessionId, MessageType.valueOf(type), text, repliedId, file, requestId, storyId);
    }

    @GetMapping(value = "/{session-id}")
    @PreAuthorize("hasAuthority('b.22')")
    @Operation(summary = "دریافت پیام ها", description = "b.22")
    @Permission(member = true, sessionId = "#sessionId")
    public Page<MessageOutputModel> getAllMessages(
            @PathVariable("session-id") String sessionId, @Validated @ParameterObject PageQueryParams queryParams) {
        return messageServiceFacade.getAll(sessionId, queryParams);
    }

    @PatchMapping(value = "/{message-id}")
    @PreAuthorize("hasAuthority('b.23')")
    @Operation(summary = "ویرایش پیام", description = "b.23")
    @Permission(sender = true, messageId = "#messageId")
    public MessageOutputModel editMessage(
            @PathVariable("message-id") String messageId, @RequestBody EditMessageInputModel inputModel) {
        return messageServiceFacade.editMessage(messageId, inputModel);
    }

    @DeleteMapping(value = "/{message-id}")
    @PreAuthorize("hasAuthority('b.24')")
    @Operation(summary = "حذف پیام", description = "b.24")
    @Permission(sender = true, messageId = "#messageId")
    public void deleteMessage(@PathVariable("message-id") String messageId) {
        messageServiceFacade.deleteMessage(messageId);
    }

    @PostMapping(value = "/seen/{message-id}")
    @PreAuthorize("hasAuthority('b.25')")
    @Operation(summary = "مشاهده پیام", description = "b.25")
    @Permission(member = true, messageId = "#messageId")
    public SeenOutputModel seenMessage(@PathVariable("message-id") String messageId) {
        return messageServiceFacade.seenMessage(messageId);
    }

    @PostMapping(value = "/reaction/{message-id}")
    @PreAuthorize("hasAuthority('b.26')")
    @Operation(summary = "ارسال واکنش برای پیام", description = "b.26")
    @Permission(member = true, messageId = "#messageId")
    public void sendReaction(@PathVariable("message-id") String messageId, @RequestBody @Valid ReactionInputModel reactionInputModel) {
        messageServiceFacade.sendReaction(messageId, reactionInputModel.getReaction());
    }

    @PostMapping(value = "/pin/{message-id}")
    @PreAuthorize("hasAuthority('b.27')")
    @Operation(summary = "پین کردن پیام", description = "b.27")
    @Permission(channelOwner = true, groupOwner = true, messageId = "#messageId", acceptableTypes = {CHANNEL, GROUP})
    public void pinMessage(@PathVariable("message-id") String messageId) {
        messageServiceFacade.pinMessage(messageId);
    }

    @PostMapping(value = "/unpin/{message-id}")
    @PreAuthorize("hasAuthority('b.28')")
    @Operation(summary = "حذف پین پیام", description = "b.28")
    @Permission(channelOwner = true, groupOwner = true, messageId = "#messageId", acceptableTypes = {CHANNEL, GROUP})
    public void unpinMessage(@PathVariable("message-id") String messageId) {
        messageServiceFacade.unpinMessage(messageId);
    }

    @GetMapping(value = "/iterative/{session-id}")
    @PreAuthorize("hasAuthority('b.29')")
    @Operation(summary = "دریافت پیام ها به صورت اشاره ای", description = "b.29")
    @Permission(member = true, sessionId = "#sessionId")
    public List<MessageOutputModel> getAllMessagesIterative(
            @PathVariable("session-id") String sessionId,
            @RequestParam(required = false) String messageId,
            @RequestParam(required = false) IterativePageState state,
            @RequestParam Integer limit
    ) {
        return messageServiceFacade.getAllMessagesIterative(sessionId, messageId, state, limit);
    }

    @PostMapping("/typing/{session-id}")
    @PreAuthorize("hasAuthority('b.30')")
    @Operation(summary = "ارسال وضعیت در حال تایپ", description = "b.30")
    @Permission(member = true, sessionId = "#sessionId")
    public void sendTypingStatus(@PathVariable("session-id") String sessionId) {
        messageServiceFacade.sendTypingStatus(sessionId);
    }

}
