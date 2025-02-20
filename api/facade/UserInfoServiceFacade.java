package org.tpl.chat.api.facade;


import org.tpl.chat.api.dto.UserInfoOutputModel;
import org.tpl.chat.api.facade.mapper.UserInfoFacadeMapper;
import org.tpl.chat.service.UserInfoService;
import org.tpl.chat.util.UserUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public record UserInfoServiceFacade(UserInfoService userInfoService,
                                    UserInfoFacadeMapper mapper) {

    public List<UserInfoOutputModel> getUsersInfo(Set<String> userIdSet) {
        return userInfoService.getByUserIdSet(userIdSet).stream().map(mapper::userInfoToUserInfoOutputModel).collect(Collectors.toList());
    }
}
