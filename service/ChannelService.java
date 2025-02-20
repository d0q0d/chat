package org.tpl.chat.service;

import org.tpl.chat.dal.model.Channel;
import org.tpl.chat.dal.model.SessionUpdateModel;
import org.springframework.web.multipart.MultipartFile;
import org.tpl.chat.dal.model.Session;

public interface ChannelService {

  Session create(Channel channel);

  Session partialUpdate(SessionUpdateModel sessionUpdateModel);

  Session updateChannelImage(MultipartFile inputModel, String sessionId);

}
