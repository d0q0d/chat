package org.tpl.chat.service.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.service.BrokerProducer;
import org.tpl.util.common.service.model.DataBaseLog;
import org.tpl.util.common.service.model.DataBaseOperationType;
import org.tpl.util.mongodbcommon.dal.entity.BaseEntity;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.tpl.util.common.service.model.DataBaseOperationType.*;

public class GenericDataBaseLogListener<T extends BaseEntity> extends AbstractMongoEventListener<T> {

    private final ObjectMapper objectMapper;
    private final BrokerProducer producer;
    private final String databaseLogsTopic;
    private final String applicationName;
    private final UserUtil userUtil;

    public GenericDataBaseLogListener(ObjectMapper objectMapper, BrokerProducer producer, String databaseLogsTopic, String applicationName, UserUtil userUtil) {
        this.objectMapper = objectMapper;
        this.producer = producer;
        this.databaseLogsTopic = databaseLogsTopic;
        this.applicationName = applicationName;
        this.userUtil = userUtil;
    }

    @Override
    public void onAfterSave(AfterSaveEvent<T> event) {
        super.onAfterSave(event);
        if (Objects.isNull(event.getSource().getVersion()) || event.getSource().getVersion().equals(0L)) produceDatabaseLog(event.getSource(), INSERT);
        else produceDatabaseLog(event.getSource(), UPDATE);
    }

    @Override
    public void onAfterDelete(AfterDeleteEvent<T> event) {
        super.onAfterDelete(event);
        produceDatabaseLogByDocument(event.getType(), event.getSource(), DELETE);
    }

    private void produceDatabaseLog(T entity, DataBaseOperationType type) {
        try {
            String data = objectMapper.writeValueAsString(entity);
            producer.produce(
                    databaseLogsTopic,
                    objectMapper.writeValueAsString(getDataBaseLogModel(entity, type, data))
            );
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void produceDatabaseLogByDocument(Class clazz, Document document, DataBaseOperationType type) {
        try {
            String data = document.toJson();
            String id = UUID.randomUUID().toString();
            DataBaseLog dataBaseLog = new DataBaseLog(
                    id,
                    String.valueOf(document.get("_id")),
                    LocalDateTime.now(),
                    userUtil.getUserId(),
                    userUtil.getIp(),
                    type,
                    null,
                    null,
                    null,
                    applicationName,
                    clazz.getName(),
                    data
            );
            producer.produce(
                    databaseLogsTopic,
                    objectMapper.writeValueAsString(dataBaseLog)
            );
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private DataBaseLog getDataBaseLogModel(BaseEntity baseEntity, DataBaseOperationType type, String data){
        String userId = userUtil.getUserId();
        String ip = userUtil.getIp();
        userId = userId == null ? baseEntity.getUpdatedBy() : userId;
        ip = ip == null ? baseEntity.getUpdatedIp() : ip;
        String id = UUID.randomUUID().toString();
        return new DataBaseLog(id, String.valueOf(baseEntity.getId()), LocalDateTime.now(), userId, ip, type, baseEntity.getVersion(), baseEntity.getCreatedDate(), baseEntity.getLastModifiedDate(), applicationName, baseEntity.getClass().getName(), data);
    }

}
