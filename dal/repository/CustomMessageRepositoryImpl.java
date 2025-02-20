package org.tpl.chat.dal.repository;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.Message;
import org.tpl.chat.service.model.IterativePageState;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomMessageRepositoryImpl implements CustomMessageRepository {

  private final MongoTemplate mongoTemplate;

  @Override
  public void seenMessage(String sessionId, String userId, LocalDateTime seenTo) {
    var query =
        new Query(
            Criteria.where("sessionId")
                .is(sessionId)
                .and("senderId")
                .ne(userId)
                .and("receiverIds")
                .ne(userId)
                .and("createdDate")
                .lte(seenTo));
    var update = new Update().set("messageStatus", "SEEN").addToSet("receiverIds", userId);
    mongoTemplate.updateMulti(query, update, Message.class);
  }

  @Override
  public List<Message> findMessagesIterative(String sessionId, LocalDateTime lastDeletedDate, LocalDateTime messageDateTime, IterativePageState state, int limit) {
    if (Objects.isNull(state) || Objects.isNull(messageDateTime)){
      Query query = new Query();
      query.limit(limit);
      query.with(Sort.by(Sort.Direction.DESC, "createdDate"));
      query.addCriteria(Criteria.where("sessionId").is(sessionId));
      if (Objects.nonNull(lastDeletedDate)){
        query.addCriteria(Criteria.where("createdDate").gt(lastDeletedDate));
      }
      query.fields().exclude("receiverIds");
      return mongoTemplate.find(query, Message.class);
    }else {
      Query query = new Query();
      query.limit(limit);
      Sort.Direction direction = state.equals(IterativePageState.PREVIOUS) ? Sort.Direction.DESC : Sort.Direction.ASC;
      query.with(Sort.by(direction, "createdDate"));
      query.addCriteria(Criteria.where("sessionId").is(sessionId));
      Criteria messageDateTimeCriteria;
      if (state.equals(IterativePageState.PREVIOUS)){
        messageDateTimeCriteria = Criteria.where("createdDate").lte(messageDateTime);
      }else {
        messageDateTimeCriteria = Criteria.where("createdDate").gte(messageDateTime);
      }
      if (Objects.nonNull(lastDeletedDate)){
        query.addCriteria(new Criteria().andOperator(messageDateTimeCriteria, Criteria.where("createdDate").gt(lastDeletedDate)));
      }else {
        query.addCriteria(messageDateTimeCriteria);
      }
      query.fields().exclude("receiverIds");
      return mongoTemplate.find(query, Message.class);
    }
  }

  @Override
  public Optional<Message> getFirstUnreadMessage(String sessionId, String userId, LocalDateTime lastDeletedDate) {
    Query query = new Query();
    query.with(Sort.by(Sort.Direction.ASC, "createdDate"));
    query.limit(1);
    query.addCriteria(Criteria.where("sessionId").is(sessionId).and("senderId").ne(userId).and("receiverIds").ne(userId));
    if (Objects.nonNull(lastDeletedDate)){
      query.addCriteria(Criteria.where("createdDate").gt(lastDeletedDate));
    }
    Message message = mongoTemplate.findOne(query, Message.class);
    return message == null ? Optional.empty() : Optional.of(message);
  }

}
