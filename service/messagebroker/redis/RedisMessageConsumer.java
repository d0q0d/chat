package org.tpl.chat.service.messagebroker.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class RedisMessageConsumer implements MessageListener {

    private final WebSocketSession webSocketSession;

    public RedisMessageConsumer(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String receivedMessage = new String(message.getBody());
            webSocketSession.sendMessage(new TextMessage(receivedMessage));
        } catch (Exception e) {
            log.info("error happened along sending message to session: {}", webSocketSession.getId());
            e.printStackTrace();
        }
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

}
