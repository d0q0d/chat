package org.tpl.chat.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.tpl.chat.api.dto.CreateGroupInputModel;
import org.tpl.chat.api.dto.SessionOutputModel;
import org.tpl.chat.api.dto.UpdateGroupInputModel;
import org.tpl.chat.api.dto.UpdateImageInputModel;
import org.tpl.chat.api.facade.GroupServiceFacade;
import org.tpl.chat.service.annotation.Permission;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat/groups")
@RequiredArgsConstructor
public class GroupController {

  private final GroupServiceFacade groupServiceFacade;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('b.61')")
  @Operation(summary = "ایجاد گروه", description = "b.61")
  public SessionOutputModel create(@Validated @ModelAttribute CreateGroupInputModel inputModel) {
    return groupServiceFacade.create(inputModel);
  }

  @PatchMapping(path = "/{session-id}")
  @PreAuthorize("hasAuthority('b.62')")
  @Operation(summary = "آپدیت اطلاعات گروه", description = "b.62")
  @Permission(groupOwner = true, sessionId = "#sessionId")
  public SessionOutputModel update(
      @Validated @RequestBody UpdateGroupInputModel inputModel,
      @PathVariable("session-id") String sessionId) {
    return groupServiceFacade.update(inputModel, sessionId);
  }

  @PutMapping(path = "/image/{session-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('b.63')")
  @Operation(summary = "آپدیت تصویر گروه", description = "b.63")
  @Permission(groupOwner = true, sessionId = "#sessionId")
  public SessionOutputModel updateGroupImage(
      @Validated @ModelAttribute UpdateImageInputModel inputModel,
      @PathVariable("session-id") String sessionId) {
    return groupServiceFacade.updateGroupImage(inputModel, sessionId);
  }

}
