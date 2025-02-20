package org.tpl.chat.dal.repository;

import org.tpl.chat.dal.model.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserInfoRepository extends CrudRepository<UserInfo, String> {
    Optional<UserInfo> findByUserId(String userId);
    List<UserInfo> findAllByUserIdIn(Set<String> userIds);
}
