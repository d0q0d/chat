package org.tpl.chat.service;

import org.tpl.chat.dal.model.Message;
import org.tpl.chat.dal.model.Session;
import org.tpl.chat.service.model.IterativePageState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface MessageService {
  Message send(Message message, MultipartFile file);

  Message save(Message message);

  Message getLastMessage(String sessionId);

  Page<Message> getMessages(String sessionId, Pageable pageable);

  List<Message> getMessagesIterative(String sessionId, String messageId, IterativePageState state, int limit);

  Message editMessage(String messageId, String content);

  Message getById(String messageId);

  Optional<Message> getOptionalById(String messageId);

  void deleteById(String messageId);

  void sendReaction(String messageId, String reaction, String userId);

  String seenMessage(String messageId);

  int getUnreadMessageCount(String userId, Session session);

  void pin(String messageId);

  void unpin(String messageId);

  void sendTypingStatus(String sessionId, String userId);

  Optional<Message> getFirstUnreadMessage(Session session, String userId);

}
