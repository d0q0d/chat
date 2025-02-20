package org.tpl.chat.dal.repository;

import org.tpl.chat.dal.model.LiveMessage;

import java.util.List;

public interface CustomLiveMessageRepository {

    List<LiveMessage> findAllByTopic(String topic);

    void deleteById(String id);

}
