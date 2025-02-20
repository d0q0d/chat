package org.tpl.chat.service.messagebroker.onechannelredis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.tpl.chat.service.model.SocketMessageModel;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OneChannelRedisMessageListener implements MessageListener {

    private final Map<String, List<WebSocketSession>> sessionsMap;
    private final ObjectMapper objectMapper;

    public OneChannelRedisMessageListener(Map<String, List<WebSocketSession>> sessionsMap, ObjectMapper objectMapper) {
        this.sessionsMap = sessionsMap;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String receivedMessage = new String(message.getBody());
            SocketMessageModel<Object> socketMessageModel = objectMapper.readValue(receivedMessage, SocketMessageModel.class);
            List<WebSocketSession> sessions = sessionsMap.get(socketMessageModel.getReceiverId());
            if (!CollectionUtils.isEmpty(sessions)){
                sessions.forEach(session -> {
                    try {
                        session.sendMessage(new TextMessage(receivedMessage));
                    }catch (Exception e){
                        log.info("error while sending message on socket. {}", e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
