package org.tpl.chat.service.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class ServerWebSocketSockJsConfig implements WebSocketConfigurer {

    private final ServerWebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(webSocketHandler, "/api/ws")
//                .setAllowedOrigins("*")
//                .withSockJS()
//                .setWebSocketEnabled(true)
//                .setHeartbeatTime(25000)
//                .setDisconnectDelay(5000)
//                .setClientLibraryUrl("/webjars/sockjs-client/1.1.2/sockjs.js")
//                .setSessionCookieNeeded(false);
        registry.addHandler(webSocketHandler, "/public/chat/ws")
                .setAllowedOrigins("*");

    }

}