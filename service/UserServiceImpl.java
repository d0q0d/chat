package org.tpl.chat.service;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.User;
import org.tpl.chat.dal.repository.UserRepository;
import org.tpl.chat.service.model.AccessPolicyModel;
import org.tpl.chat.service.remote.model.UserManagementProfile;
import org.tpl.chat.service.remote.usermanagement.UsermanagementApiAdapter;
import org.tpl.chat.service.remote.usermanagement.model.RoleModel;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.service.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UsermanagementApiAdapter userManagementAuthAdapter;
    private final UserRepository repository;
    private final UserUtil userUtil;

    @Override
    public User getById(String userId) {
        return repository.findById(userId).orElseGet(() -> {
            UserManagementProfile profile;
            try {
                profile = userManagementAuthAdapter.getProfile(userId);
            }catch (NotFoundException e){
                e.printStackTrace();
                return null;
            }
            User user = new User(profile.getId(), profile.getFirstName(), profile.getLastName(), profile.getPersonnelPhotoUrl());
            return repository.save(user);
        });
    }

    @Override
    public List<User> getByIds(Set<String> userIds) {
        return fetchUsers(userIds);
    }

    @Override
    public Map<String, User> getMapByIds(Set<String> userIds) {
        List<User> users = fetchUsers(userIds);
        return users.stream().collect(Collectors.toMap(User::getUserId, user -> user));
    }

    @Override
    public Boolean hasAccessBasedOnRole(String userId) {
        return userManagementAuthAdapter.hasAccessBasedOnRole(userId);
    }

    @Override
    public RoleModel getCurrentRole() {
        return userManagementAuthAdapter.getRoleById(userUtil.getCurrentRoleId());
    }

    @Override
    public AccessPolicyModel getAccessPolicyModelByCurrentRole() {
        AccessPolicyModel accessPolicyModel = userManagementAuthAdapter.getAccessPolicyModelByRoleId(userUtil.getCurrentRoleId());
        if (accessPolicyModel.getOwnFormationCodes() == null) accessPolicyModel.setOwnFormationCodes(new ArrayList<>());
        if (accessPolicyModel.getOtherFormationCodes() == null) accessPolicyModel.setOtherFormationCodes(new ArrayList<>());
        return accessPolicyModel;
    }

    private List<User> fetchUsers(Set<String> userIds) {
        Set<String> notExistsUsers = new HashSet<>();
        List<User> users = new ArrayList<>();
        userIds.forEach(id -> repository.findById(id).ifPresentOrElse(users::add, () -> notExistsUsers.add(id)));
        if (!notExistsUsers.isEmpty()) {
            List<User> userList = userManagementAuthAdapter.getProfilesByList(notExistsUsers).stream()
                    .map(profile -> {
                        User user = new User(profile.getId(), profile.getFirstName(), profile.getLastName(), profile.getPersonnelPhotoUrl());
                        return repository.save(user);
                    })
                    .collect(Collectors.toList());
            users.addAll(userList);
        }
        return users;
    }

}

