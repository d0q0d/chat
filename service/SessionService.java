package org.tpl.chat.service;

import org.tpl.chat.dal.model.GroupAndChannelCountOutputModel;
import org.tpl.chat.dal.model.Session;
import org.tpl.chat.dal.model.SessionType;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.service.model.MessagePreviewModel;
import org.tpl.chat.service.model.SessionRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SessionService {
  Session getById(String sessionId);

  Session getByMembersAndType(Set<String> members, SessionType sessionType);

  Session save(Session session);

  void deleteById(String sessionId, String userId);

  List<Session> getAll(String userId);

  Page<MemberModel> getMembers(String sessionId, Pageable pageable);

  SessionRole getRoleFromSessionAndUserId(Session session, String userId);

  MessagePreviewModel getMessagePreview(Session session);

  GroupAndChannelCountOutputModel getGroupAndChannelCount(String userId);

  GroupAndChannelCountOutputModel getGroupAndChannelCountCreatedByUser(String userId, LocalDateTime from, LocalDateTime to);

  int getP2PCountCreatedByUser(String userId, LocalDateTime from, LocalDateTime to);

}
