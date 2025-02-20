package org.tpl.chat.service.messagebroker;

import org.tpl.chat.service.model.SocketMessageModel;

public interface ProducerHandler {
    <T> void sendAndSaveInMemory(String topicId, SocketMessageModel<T> message);

    void justSend(String topic, String message);
}
