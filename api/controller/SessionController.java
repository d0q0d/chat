package org.tpl.chat.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.tpl.chat.api.dto.SessionOutputModel;
import org.tpl.chat.api.dto.UserInfoInputModel;
import org.tpl.chat.api.dto.UserInfoOutputModel;
import org.tpl.chat.api.facade.SessionServiceFacade;
import org.tpl.chat.dal.model.UserInfo;
import org.tpl.chat.service.annotation.Permission;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.util.common.service.model.PageQueryParams;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.tpl.chat.dal.model.SessionType.*;

@RestController
@RequestMapping("/api/v1/chat/sessions")
@RequiredArgsConstructor
public class SessionController {

  private final SessionServiceFacade sessionServiceFacade;

  @GetMapping
  @PreAuthorize("hasAuthority('b.41')")
  @Operation(summary = "دربافت لیست نشست ها", description = "b.41")
  public List<SessionOutputModel> getAll() {
    return sessionServiceFacade.getAll();
  }

  @GetMapping(value = "/{session-id}")
  @PreAuthorize("hasAuthority('b.42')")
  @Operation(summary = "دریافت نشست با آیدی", description = "b.42")
  @Permission(member = true, sessionId = "#sessionId")
  public SessionOutputModel get(@PathVariable("session-id") String sessionId) {
    return sessionServiceFacade.get(sessionId);
  }

  @DeleteMapping(value = "/{session-id}")
  @PreAuthorize("hasAuthority('b.43')")
  @Operation(summary = "حذف نشست با آیدی", description = "b.43")
  @Permission(member = true, sessionId = "#sessionId")
  public void delete(@PathVariable("session-id") String sessionId) {
    sessionServiceFacade.delete(sessionId);
  }

  @GetMapping("/members/{session-id}")
  @PreAuthorize("hasAuthority('b.44')")
  @Operation(summary = "دریافت لیست عضوها", description = "b.44")
  @Permission(member = true, sessionId = "#sessionId")
  public Page<MemberModel> getMembers(@PathVariable("session-id") String sessionId, @Validated @ParameterObject PageQueryParams queryParams) {
    return sessionServiceFacade.getMembers(sessionId, queryParams);
  }

  @PostMapping(value = "/pin/{session-id}")
  @PreAuthorize("hasAuthority('b.45')")
  @Operation(summary = "پین کردن نشست", description = "b.45")
  @Permission(member = true, sessionId = "#sessionId", acceptableTypes = {CHANNEL, GROUP, P2P})
  public void pinSession(@PathVariable("session-id") String sessionId) {
    sessionServiceFacade.pinSession(sessionId);
  }

  @PostMapping(value = "/unpin/{session-id}")
  @PreAuthorize("hasAuthority('b.46')")
  @Operation(summary = "حذف پین نشست", description = "b.46")
  @Permission(member = true , sessionId = "#sessionId", acceptableTypes = {CHANNEL, GROUP, P2P})
  public void unpinSession(@PathVariable("session-id") String sessionId) {
    sessionServiceFacade.unpinSession(sessionId);
  }


}
