package org.tpl.chat.dal.repository;

import org.tpl.chat.dal.model.Message;
import org.tpl.chat.dal.model.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

public interface MessageRepository extends MongoRepository<Message, String> , CustomMessageRepository{
  Message save(Message message);

  Message findTopBySessionIdOrderByCreatedDateDesc(String sessionId);

  @Query(fields = "{receiverIds:0}")
  Page<Message> findAllBySessionIdAndCreatedDateAfter(
      String sessionId, LocalDateTime localDateTime, Pageable pageable);

  @Query(fields = "{receiverIds:0}")
  Page<Message> findAllBySessionId(String sessionId, Pageable pageable);

  int countBySessionIdAndSenderIdNotAndReceiverIdsNotContaining(String sessionId,String userId, String userId2);

  int countBySessionIdAndSenderIdNotAndReceiverIdsNotContainingAndCreatedDateAfter(String sessionId,String userId, String userId2, LocalDateTime createdDate);

}
