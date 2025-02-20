package org.tpl.chat.dal.repository;


import java.util.Optional;
import java.util.Set;
import org.tpl.chat.dal.model.Session;
import org.tpl.chat.dal.model.SessionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SessionRepository extends MongoRepository<Session, String>, CustomSessionRepository {
    Session getById(String id);
    @Query(value = "{_id:{$oid:?0}}", fields = "{members:0}")
    Optional<Session> findByIdAndExcludeMembers(String id);
    @Query("{$and:[{'members':{$all:?0}}, {'members':{$size:?1}}, {'sessionType':?2}]}")
    Session getByMembersAndSizeAndSessionType(Set<String> members, Integer size, SessionType sessionType);
    int countBySessionTypeAndMembersIn(SessionType sessionType, String userId);
}
