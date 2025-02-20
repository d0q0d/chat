//package org.tpl.chat.service.log;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.tpl.chat.dal.model.Session;
//import org.tpl.chat.util.UserUtil;
//import org.tpl.util.common.service.BrokerProducer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SessionDataBaseLogListener extends GenericDataBaseLogListener<Session> {
//    public SessionDataBaseLogListener(
//            ObjectMapper objectMapper,
//            BrokerProducer producer,
//            @Value("${broker.topics.database.logs}") String databaseLogsTopic,
//            @Value("${spring.application.name}") String applicationName,
//            UserUtil userUtil
//    ) {
//        super(objectMapper, producer, databaseLogsTopic, applicationName, userUtil);
//    }
//}
