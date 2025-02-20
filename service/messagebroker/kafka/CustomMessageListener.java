package org.tpl.chat.service.messagebroker.kafka;//package com.project.chat.messagebroker.kafka;
//
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.listener.MessageListener;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//
//import java.io.IOException;
//
//@AllArgsConstructor
//@Slf4j
//public class CustomMessageListener implements MessageListener<String, String> {
//
//    private WebSocketSession webSocketSession;
//
//    public void onMessage(ConsumerRecord<String, String> record) {
//        try {
//            webSocketSession.sendMessage(new TextMessage(record.value()));
//        } catch (IOException e) {
//            log.info("error happened along sending message to session: {}", webSocketSession.getId());
//            e.printStackTrace();
//        }
//    }
//
//}
