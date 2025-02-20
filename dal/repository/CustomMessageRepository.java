package org.tpl.chat.dal.repository;

import org.tpl.chat.dal.model.Message;
import org.tpl.chat.service.model.IterativePageState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomMessageRepository {
    void seenMessage(String sessionId, String userId, LocalDateTime seenTo);
    List<Message> findMessagesIterative(String sessionId, LocalDateTime lastDeletedDate, LocalDateTime messageDateTime, IterativePageState state, int limit);
    Optional<Message> getFirstUnreadMessage(String sessionId, String userId, LocalDateTime lastDeletedDate);
}
