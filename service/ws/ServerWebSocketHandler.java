package org.tpl.chat.service.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.tpl.chat.service.UserInfoService;
import org.tpl.chat.service.messagebroker.ListenerHandler;
import org.tpl.chat.service.model.SocketMessageModel;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.service.exception.UnauthorizedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

import static org.tpl.chat.dal.model.OnlineStatusEnum.*;

@Component
@RequiredArgsConstructor
public class ServerWebSocketHandler extends TextWebSocketHandler implements SubProtocolCapable {

    private final ListenerHandler listenerHandler;
    private final UserUtil userUtil;
    private final JwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;
    private final WebSocketController controller;
    private final UserInfoService userInfoService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String token = getTokenFromQuery(session);
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(token);
        } catch (Exception e) {
            closeSocketAlongWithSendingMessageForInvalidToken(session);
            throw e;
        }
        String userId = userUtil.getUserIdFromJwt(jwt);
        session.getAttributes().put("userId", userId);
        listenerHandler.createAndRegisterListenerForWebsocket(
                session,
                userId
        );
        userInfoService.changeOnlineStatus(userId, ONLINE);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = (String) session.getAttributes().get("userId");
        listenerHandler.removeListenerByListenerIdAndSession(userId, session);
        userInfoService.changeOnlineStatus(userId, OFFLINE);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        controller.handle(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.out.printf("Server transport error: {}: %s", exception.getMessage());
    }

    @Override
    public List<String> getSubProtocols() {
        return Collections.singletonList("subprotocol.demo.websocket");
    }

    private String getTokenFromQuery(WebSocketSession session) {
        String query = session.getUri().getQuery();
        Map<String, String> params = parseQueryParams(query);
        if (params.containsKey("Authorization")) {
            return params.get("Authorization");
        }
        throw new UnauthorizedException();
    }

    private void closeSocketAlongWithSendingMessageForInvalidToken(WebSocketSession session) throws IOException {
        SocketMessageModel<Void> messageModel = new SocketMessageModel<>();
        messageModel.setType(SocketMessageModel.Type.INVALID_TOKEN);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageModel)));
        session.close();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    queryParams.put(key, value);
                }
            }
        }
        return queryParams;
    }

}
