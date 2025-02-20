//package org.tpl.chat.service.messagebroker.kafka;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import org.tpl.chat.dal.model.LiveMessage;
//import org.tpl.chat.service.LiveMessageService;
//import org.tpl.chat.service.messagebroker.ListenerHandler;
//import org.tpl.chat.service.messagebroker.ProducerHandler;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//import org.springframework.web.socket.WebSocketSession;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//@RequiredArgsConstructor
//public class KafkaListenerHandler implements ListenerHandler {
//
//    private final Map<String, List<WebSocketSession>> sessionsMap = new ConcurrentHashMap<>();
//    private final KafkaTemplate<String, String> kafkaTemplate;
//    private final LiveMessageService liveMessageService;
//    private final ProducerHandler producerHandler;
//    @Value("${broker.topics.live.messages}")
//    private String liveMessageTopic;
//
//    @Override
//    public void createAndRegisterListenerForWebsocket(WebSocketSession session, String receiverId) {
//        String topicWithReceiverId = liveMessageTopic + receiverId;
//        Objects.requireNonNull(session);
//        Objects.requireNonNull(receiverId);
//        addSessionToMap(session, receiverId);
//        sendSavedLiveMessages(topicWithReceiverId);
//    }
//
//    private void addSessionToMap(WebSocketSession session, String receiverId) {
//        sessionsMap.compute(receiverId, (key, sessions) -> {
//            if (sessions == null) return new ArrayList<>(){{add(session);}};
//            sessions.add(session);
//            return sessions;
//        });
//    }
//
//    @Override
//    public void removeListenerByListenerIdAndSession(String receiverId, WebSocketSession session) {
//        List<WebSocketSession> sessions = sessionsMap.get(receiverId);
//        if (Objects.nonNull(sessions)){
//            synchronized (sessions) {
//                sessions.removeIf(webSocketSession -> webSocketSession.getId().equals(session.getId()));
//            }
//        }
//    }
//
//    private void sendSavedLiveMessages(String topicWithReceiverId) {
//        List<LiveMessage> messages = liveMessageService.getAllByTopic(topicWithReceiverId);
//        if (CollectionUtils.isEmpty(messages)) return;
//        messages.stream()
//                .sorted(Comparator.comparing(LiveMessage::getDateTime))
//                .forEach(liveMessage -> producerHandler.justSend(null, liveMessage.getMessage()));
//    }
//
//    @SneakyThrows
//    @KafkaListener(
//            topics = "${broker.topics.authority}",
//            groupId = "${spring.kafka.consumer.group-id}"
//    )
//    public void consumeAuthorityMessage(String message) {
//        kafkaTemplate.
//        log.info("kafka message consumeAuthorityMessage, message = {}", message);
//        Map<String, String> authorities = objectMapper.readValue(message, new TypeReference<>() {});
//        adapter.updateAuthorities(authorities);
//    }
//
//}
