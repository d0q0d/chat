package org.tpl.chat.service.messagebroker;

import org.springframework.web.socket.WebSocketSession;

import java.util.List;

public interface ListenerHandler {

    void createAndRegisterListenerForWebsocket(WebSocketSession session, String receiverId);
    void removeListenerByListenerIdAndSession(String listenerId, WebSocketSession session);

}
