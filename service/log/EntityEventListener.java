package org.tpl.chat.service.log;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.mongodbcommon.dal.entity.BaseEntity;
import org.springframework.data.mongodb.core.mapping.event.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EntityEventListener extends AbstractMongoEventListener<BaseEntity> {

    private final UserUtil userUtil;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<BaseEntity> event) {
        super.onBeforeConvert(event);
        BaseEntity baseEntity = event.getSource();
        if (Objects.nonNull(baseEntity.getId())) beforeUpdate(baseEntity);
        else beforeInsert(baseEntity);
    }

    private void beforeInsert(BaseEntity baseEntity){
        if (!baseEntity.isAuditingIsManuallyFilled()){
            baseEntity.setCreatedBy(userUtil.getUserId());
            baseEntity.setUpdatedBy(userUtil.getUserId());
            baseEntity.setCreatedIp(userUtil.getIp());
            baseEntity.setUpdatedIp(userUtil.getIp());
        }
    }

    private void beforeUpdate(BaseEntity baseEntity){
        if (!baseEntity.isAuditingIsManuallyFilled()){
            baseEntity.setUpdatedBy(userUtil.getUserId());
            baseEntity.setUpdatedIp(userUtil.getIp());
        }
    }

}
