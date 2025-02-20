//package org.tpl.chat.service.messagebroker.kafka;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.tpl.chat.dal.model.LiveMessage;
//import org.tpl.chat.service.LiveMessageService;
//import org.tpl.chat.service.messagebroker.ProducerHandler;
//import org.tpl.chat.service.model.SocketMessageModel;
//import org.tpl.util.common.service.BrokerProducer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//import static org.tpl.chat.service.model.SocketMessageModel.Type.*;
//
//@Service
//@RequiredArgsConstructor
//public class KafkaProducerHandler implements ProducerHandler {
//
//  private final BrokerProducer producer;
//  private final LiveMessageService liveMessageService;
//  private final ObjectMapper objectMapper;
//  private final List<SocketMessageModel.Type> types = List.of(MESSAGE, SEEN, DELETED, EDITED, SESSION_DELETED, SESSION_EDITED, REACTION, ADDED_TO_OWNERS, REMOVED_FROM_OWNERS);
//
//  @Value("${broker.topics.live.messages}")
//  private String liveMessageTopic;
//
//  @Override
//  public <T> void sendAndSaveInMemory(String receiverId, SocketMessageModel<T> message) {
//    try {
//      String id = UUID.randomUUID().toString();
//      message.setId(id);
//      String stringMessage = objectMapper.writeValueAsString(message);
//      String topicWithReceiverId = liveMessageTopic + "-" + receiverId;
//      if (types.contains(message.getType())){
//        liveMessageService.save(new LiveMessage(topicWithReceiverId + ":" + id, LocalDateTime.now(), topicWithReceiverId, stringMessage));
//      }
//      producer.produce(liveMessageTopic, stringMessage);
//    } catch (JsonProcessingException e) {
//      e.printStackTrace();
//    }
//  }
//
//  @Override
//  public void justSend(String topic, String message) {
//    producer.produce(liveMessageTopic, message);
//  }
//
//}
