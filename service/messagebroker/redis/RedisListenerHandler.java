package org.tpl.chat.service.messagebroker.redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.tpl.chat.dal.model.LiveMessage;
import org.tpl.chat.service.LiveMessageService;
import org.tpl.chat.service.messagebroker.ListenerHandler;
import org.tpl.chat.service.messagebroker.ProducerHandler;
import org.tpl.chat.service.messagebroker.onechannelredis.OneChannelRedisMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value="active.message.broker", havingValue = "redis")
public class RedisListenerHandler implements ListenerHandler {

    private final RedisMessageListenerContainer container;
    private final Map<String, List<MessageListener>> sessionsMessageListenersMap = new ConcurrentHashMap<>();
    private final LiveMessageService liveMessageService;
    private final ProducerHandler producerHandler;
    @Value("${redis.topic.prefix}")
    private String topicPrefix;

    @PostConstruct
    public void init(){
        log.info("redis listener initialized.");
    }

    @PreDestroy
    @SneakyThrows
    public void preDestroy(){
        sessionsMessageListenersMap.forEach((listenerId, messageListener) -> {
            removeListenersByListenerIdAlongWithClosingRelatedSessions(listenerId);
        });
        log.info("all redis listeners removed.");
    }

    @Override
    public void createAndRegisterListenerForWebsocket(WebSocketSession session, String receiverId) {
        String topic = topicPrefix + receiverId;
        List<String> topics = List.of(topic);
        Objects.requireNonNull(session);
        Objects.requireNonNull(topics);
        Objects.requireNonNull(receiverId);
        RedisMessageConsumer redisMessageConsumer = new RedisMessageConsumer(session);
        List<ChannelTopic> channelTopics = topics.stream().map(ChannelTopic::new).collect(Collectors.toList());
        initializeListener(receiverId, redisMessageConsumer, channelTopics);
        sendSavedLiveMessages(topic);
    }

    @Override
    public void removeListenerByListenerIdAndSession(String listenerId, WebSocketSession session) {
        List<MessageListener> listeners = sessionsMessageListenersMap.get(listenerId);
        if (Objects.nonNull(listeners)){
            synchronized (listeners) {
                Iterator<MessageListener> iterator = listeners.iterator();
                while (iterator.hasNext()){
                    MessageListener messageListener = iterator.next();
                    RedisMessageConsumer redisMessageConsumer = (RedisMessageConsumer) messageListener;
                    if (redisMessageConsumer.getWebSocketSession().getId().equals(session.getId())){
                        container.removeMessageListener(messageListener);
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void removeListenersByListenerIdAlongWithClosingRelatedSessions(String listenerId) {
        List<MessageListener> messageListeners = sessionsMessageListenersMap.get(listenerId);
        if (Objects.nonNull(messageListeners)){
            messageListeners.forEach(messageListener -> {
                RedisMessageConsumer redisMessageConsumer = (RedisMessageConsumer) messageListener;
                try {
                    redisMessageConsumer.getWebSocketSession().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        removeListenersByListenerId(listenerId);
    }

    public void removeListenersByListenerId(String listenerId) {
        List<MessageListener> messageListeners = sessionsMessageListenersMap.get(listenerId);
        if (Objects.nonNull(messageListeners)){
            messageListeners.forEach(container::removeMessageListener);
            sessionsMessageListenersMap.remove(listenerId);
        }
    }

    private void initializeListener(String listenerId, RedisMessageConsumer redisMessageConsumer, List<ChannelTopic> channelTopics) {
//        removeOtherListenersAlongWithClosingSession(listenerId);
        sessionsMessageListenersMap.compute(listenerId, (key, listeners) -> {
            if (listeners == null) return new ArrayList<>(){{add(redisMessageConsumer);}};
            listeners.add(redisMessageConsumer);
            return listeners;
        });
        container.addMessageListener(redisMessageConsumer, channelTopics);
    }

    private void sendSavedLiveMessages(String topic) {
        List<LiveMessage> messages = liveMessageService.getAllByTopic(topic);
        if (CollectionUtils.isEmpty(messages)) return;
        messages.stream()
                .sorted(Comparator.comparing(LiveMessage::getDateTime))
                .forEach(liveMessage -> producerHandler.justSend(topic, liveMessage.getMessage()));
    }

}