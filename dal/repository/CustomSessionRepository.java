package org.tpl.chat.dal.repository;

import org.tpl.chat.dal.model.Session;
import org.tpl.chat.dal.model.SessionType;
import org.tpl.chat.service.model.MemberModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomSessionRepository {
    List<Session> findAllByMembers(String userId);
    Page<MemberModel> getMembers(String sessionId, Pageable pageable);
    long getMembersCount(String sessionId);
    long getOnlineMembersCount(String sessionId);
    int countBySessionTypeAndCreatedByAndCreatedDateBetween(SessionType sessionType, String userId, LocalDateTime from, LocalDateTime to);
}
