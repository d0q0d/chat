package org.tpl.chat.dal.repository;

import org.tpl.chat.dal.model.LiveMessage;
import org.springframework.data.repository.CrudRepository;

public interface LiveMessageRepository extends CrudRepository<LiveMessage, String> {

}
