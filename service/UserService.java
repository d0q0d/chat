package org.tpl.chat.service;

import org.tpl.chat.dal.model.User;
import org.tpl.chat.service.model.AccessPolicyModel;
import org.tpl.chat.service.remote.usermanagement.model.RoleModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

  User getById(String userid);

  List<User> getByIds(Set<String> userIds);

  Map<String, User> getMapByIds(Set<String> userIds);

  Boolean hasAccessBasedOnRole(String userId);

  RoleModel getCurrentRole();
  
  AccessPolicyModel getAccessPolicyModelByCurrentRole();

}
