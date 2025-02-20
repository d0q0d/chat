package org.tpl.chat.service.messagebroker.onechannelredis;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.tpl.chat.dal.model.LiveMessage;
import org.tpl.chat.service.LiveMessageService;
import org.tpl.chat.service.messagebroker.ListenerHandler;
import org.tpl.chat.service.messagebroker.ProducerHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value="active.message.broker", havingValue = "one-channel-redis")
public class OneChannelRedisListenerHandler implements ListenerHandler {

    private final RedisMessageListenerContainer container;
    private final Map<String, List<WebSocketSession>> sessionsMap = new ConcurrentHashMap<>();
    private final LiveMessageService liveMessageService;
    private final ProducerHandler producerHandler;
    @Value("${redis.topic.messages}")
    private String messageTopic;
    private final ObjectMapper objectMapper;
    private MessageListener listener;

    @PostConstruct
    public void init(){
        listener = new OneChannelRedisMessageListener(sessionsMap, objectMapper);
        List<ChannelTopic> channelTopics = new ArrayList<>(){{add(new ChannelTopic(messageTopic));}};
        container.addMessageListener(listener, channelTopics);
        log.info("redis listener one-channel-redis initialized.");
    }

    @PreDestroy
    @SneakyThrows
    public void preDestroy(){
        container.removeMessageListener(listener);
        log.info("redis listener removed.");
    }

    @Override
    public void createAndRegisterListenerForWebsocket(WebSocketSession session, String receiverId) {
        String topicWithReceiverId = messageTopic + receiverId;
        Objects.requireNonNull(session);
        Objects.requireNonNull(receiverId);
        addSessionToMap(session, receiverId);
        sendSavedLiveMessages(topicWithReceiverId);
    }

    @Override
    public void removeListenerByListenerIdAndSession(String receiverId, WebSocketSession session) {
        List<WebSocketSession> sessions = sessionsMap.get(receiverId);
        if (Objects.nonNull(sessions)) {
            synchronized (sessions) {
                sessions.removeIf(webSocketSession -> webSocketSession.getId().equals(session.getId()));
                log.info("session removed: {} userId:{}", receiverId, receiverId);
            }
        }
    }

    private void sendSavedLiveMessages(String topic) {
        List<LiveMessage> messages = liveMessageService.getAllByTopic(topic);
        if (CollectionUtils.isEmpty(messages)) return;
        messages.stream()
                .sorted(Comparator.comparing(LiveMessage::getDateTime))
                .forEach(liveMessage -> producerHandler.justSend(topic, liveMessage.getMessage()));
    }

    private void addSessionToMap(WebSocketSession session, String receiverId) {
        sessionsMap.compute(receiverId, (key, sessions) -> {
            if (sessions == null) return new ArrayList<>() {{
                add(session);
            }};
            sessions.add(session);
            log.info("session added: {} userId: {}", receiverId, receiverId);
            return sessions;
        });
    }

}