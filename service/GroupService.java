package org.tpl.chat.service;

import org.tpl.chat.dal.model.Group;
import org.tpl.chat.dal.model.Session;
import org.tpl.chat.dal.model.SessionUpdateModel;
import org.springframework.web.multipart.MultipartFile;

public interface GroupService {
    Session create(Group group);

    Session partialUpdate(SessionUpdateModel inputModel);

    Session updateGroupImage(MultipartFile file, String sessionId);
}
