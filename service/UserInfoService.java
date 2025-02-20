package org.tpl.chat.service;

import org.tpl.chat.dal.model.OnlineStatusEnum;
import org.tpl.chat.dal.model.UserInfo;

import java.util.List;
import java.util.Set;

public interface UserInfoService {
    void changeOnlineStatus(String userId, OnlineStatusEnum onlineStatus);
    UserInfo getByUserId(String userId);
    List<UserInfo> getByUserIdSet(Set<String> userIdSet);
    void pinSession(String userId, String sessionId);
    void unpinSession(String userId, String sessionId);
}
