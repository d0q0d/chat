package org.tpl.chat.service;

import org.tpl.chat.dal.model.LiveMessage;

import java.util.List;

public interface LiveMessageService {

    LiveMessage save(LiveMessage liveMessage);

    List<LiveMessage> getAllByTopic(String topic);

    void deleteById(String id);

}
