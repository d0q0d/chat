package org.tpl.chat.service;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.OnlineStatusEnum;
import org.tpl.chat.dal.model.PinedSession;
import org.tpl.chat.dal.model.UserInfo;
import org.tpl.chat.dal.repository.UserInfoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {

    private final UserInfoRepository repository;

    @Override
    public void changeOnlineStatus(String userId, OnlineStatusEnum onlineStatus) {
        var userInfo = repository.findByUserId(userId).orElseGet(UserInfo::new);
        userInfo.setOnlineStatus(onlineStatus);
        userInfo.setUserId(userId);
        userInfo.setLastSeen(LocalDateTime.now());
        repository.save(userInfo);
    }

    @Override
    public UserInfo getByUserId(String userId) {
        return repository.findByUserId(userId).orElse(null);
    }

    @Override
    public List<UserInfo> getByUserIdSet(Set<String> userIdSet) {
        return repository.findAllByUserIdIn(userIdSet);
    }

    @Override
    public void pinSession(String userId, String sessionId) {
        var userInfo = repository.findByUserId(userId);
        if (userInfo.isPresent()) pinSession(sessionId, userInfo);
        else createNewUserInfoAndPinSession(userId, sessionId);
    }

    @Override
    public void unpinSession(String userId, String sessionId) {
        var userInfo = repository.findByUserId(userId);
        userInfo.ifPresent(info -> {
            if (info.getPinedSessionsSet() == null) info.setPinedSessionsSet(new HashSet<>());
            info.getPinedSessionsSet().removeIf(pinedSession -> pinedSession.getSessionId().equals(sessionId));
            repository.save(info);
        });
    }

    private void createNewUserInfoAndPinSession(String userId, String sessionId) {
        var newUserInfo = new UserInfo();
        newUserInfo.setUserId(userId);
        newUserInfo.setPinedSessionsSet(Set.of(new PinedSession(sessionId, LocalDateTime.now())));
        repository.save(newUserInfo);
    }

    private void pinSession(String sessionId, Optional<UserInfo> userInfoOptional) {
        var userInfo = userInfoOptional.get();
        if (userInfo.getPinedSessionsSet() == null) userInfo.setPinedSessionsSet(new HashSet<>());
        var pinedSessionOptional = userInfo.getPinedSessionsSet().stream().filter(pinedSession -> pinedSession.getSessionId().equals(sessionId)).findFirst();
        if (pinedSessionOptional.isEmpty()){
            userInfo.getPinedSessionsSet().add(new PinedSession(sessionId, LocalDateTime.now()));
            repository.save(userInfo);
        }
    }

}
