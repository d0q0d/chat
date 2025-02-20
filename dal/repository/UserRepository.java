package org.tpl.chat.dal.repository;

import org.tpl.chat.dal.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
}
