package org.tpl.chat.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.tpl.chat.api.dto.UserInfoInputModel;
import org.tpl.chat.api.dto.UserInfoOutputModel;
import org.tpl.chat.api.facade.UserInfoServiceFacade;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/user-info")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoServiceFacade userInfoServiceFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('b.100')")
    @Operation(summary = "دریافت لیست وضعیت کاربر ها", description = "b.100")
    public List<UserInfoOutputModel> getUsersInfo(@Validated @ParameterObject UserInfoInputModel inputModel) {
        return userInfoServiceFacade.getUsersInfo(inputModel.getUserIdSet());
    }

}
