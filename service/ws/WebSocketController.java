package org.tpl.chat.service.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.tpl.chat.service.LiveMessageService;
import org.tpl.chat.service.messagebroker.ProducerHandler;
import org.tpl.chat.service.model.SocketMessageModel;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.function.BiConsumer;

import static org.tpl.chat.service.model.SocketMessageModel.Type.*;

@Component
@RequiredArgsConstructor
public class WebSocketController {

    private final ObjectMapper objectMapper;
    private final ProducerHandler producerHandler;
    private final LiveMessageService liveMessageService;
    private final Map<SocketMessageModel.Type, BiConsumer<WebSocketSession, SocketMessageModel>> consumerMap = Map.of(
            PING, this::sendPong,
            ACKNOWLEDGE, this::handleAcknowledgment
    );

    public void handle(WebSocketSession session, TextMessage message){
        try {
            var input = objectMapper.readValue(message.getPayload(), SocketMessageModel.class);
            BiConsumer<WebSocketSession, SocketMessageModel> consumer = consumerMap.get(input.getType());
            if (consumer != null) consumer.accept(session, input);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
    }

    private void sendPong(WebSocketSession session, SocketMessageModel input){
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null){
            SocketMessageModel<Void> output = new SocketMessageModel<>();
            output.setType(PONG);
            producerHandler.sendAndSaveInMemory(userId, output);
        }
    }

    private void handleAcknowledgment(WebSocketSession session, SocketMessageModel input){
        liveMessageService.deleteById(input.getId());
    }

}
