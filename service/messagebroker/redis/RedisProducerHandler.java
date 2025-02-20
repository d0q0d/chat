package org.tpl.chat.service.messagebroker.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.LiveMessage;
import org.tpl.chat.service.LiveMessageService;
import org.tpl.chat.service.messagebroker.ProducerHandler;
import org.tpl.chat.service.model.SocketMessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.tpl.chat.service.model.SocketMessageModel.Type.*;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value="active.message.broker", havingValue = "redis")
public class RedisProducerHandler implements ProducerHandler {

  private final RedisTemplate<String, String> redisTemplate;
  private final LiveMessageService liveMessageService;
  private final ObjectMapper objectMapper;
  private final List<SocketMessageModel.Type> types = List.of(MESSAGE, SEEN, DELETED, EDITED, SESSION_DELETED, SESSION_EDITED, REACTION, ADDED_TO_OWNERS, REMOVED_FROM_OWNERS, ADDED_TO_MEMBERS, REMOVED_FROM_MEMBERS);

  @Value("${redis.topic.prefix}")
  private String topicPrefix;

  @Override
  public <T> void sendAndSaveInMemory(String topicId, SocketMessageModel<T> message) {
    try {
      String id = UUID.randomUUID().toString();
      message.setId(id);
      String stringMessage = objectMapper.writeValueAsString(message);
      String topic = topicPrefix + topicId;
      if (types.contains(message.getType())){
        liveMessageService.save(new LiveMessage(topic + ":" + id, LocalDateTime.now(), topic, stringMessage));
      }
      redisTemplate.convertAndSend(topic, stringMessage);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void justSend(String topic, String message) {
    redisTemplate.convertAndSend(topic, message);
  }

}
