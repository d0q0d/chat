package org.tpl.chat.api.facade.mapper;

import org.mapstruct.Mapper;
import org.tpl.chat.api.dto.UserInfoOutputModel;
import org.tpl.chat.dal.model.UserInfo;

@Mapper(componentModel = "spring")
public interface UserInfoFacadeMapper {
    UserInfoOutputModel userInfoToUserInfoOutputModel(UserInfo userInfo);
}
