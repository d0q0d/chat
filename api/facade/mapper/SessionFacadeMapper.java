package org.tpl.chat.api.facade.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tpl.chat.api.dto.SessionOutputModel;
import org.tpl.chat.dal.model.Session;

@Mapper(componentModel = "spring")
public interface SessionFacadeMapper {

  @Mapping(source = "extraInfo.owners", ignore = true, target = "extraInfo.owners")
  SessionOutputModel sessionToSessionOutputModel(Session session);
}
