package org.tpl.chat.dal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.util.mongodbcommon.dal.entity.BaseEntity;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@CompoundIndexes({
        @CompoundIndex(name = "createdDate", def = "{createdDate:-1}")
})
@Document(collection = "story")
public class Story extends BaseEntity {

  @Indexed
  private String senderOrganizationCode;
  @Indexed
  private String senderFormationCode;
  private String senderRoleCode;
  @Indexed
  private String senderId;
  private String content;
  private StoryType type;
  private String url;

  private List<String> receiverIds;

  private Map<String, String> reactions;
  @Transient
  private MemberModel sender;
//  @Transient it is needed in view model
  private Boolean seen;

}
