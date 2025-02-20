package org.tpl.chat.dal.repository;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.OnlineStatusEnum;
import org.tpl.chat.dal.model.Session;
import org.tpl.chat.dal.repository.view.CountViewModel;
import org.tpl.chat.service.model.MemberModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
@RequiredArgsConstructor
public class CustomSessionRepositoryImpl implements CustomSessionRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Session> findAllByMembers(String userId) {
        var query =
                new Query(
                        new Criteria()
                                .andOperator(
                                        Criteria.where("members").is(userId),
                                        new Criteria("sessionDeleteModelList")
                                                .not()
                                                .elemMatch(
                                                        Criteria.where("userId")
                                                                .is(userId)
                                                                .and("isDeleted")
                                                                .is(true)
                                                )
                                )
                );
        query.fields().exclude("members");
        return mongoTemplate.find(query, Session.class);
    }

    @Override
    public Page<MemberModel> getMembers(String sessionId, Pageable pageable) {
        List<AggregationOperation> aggregationOperations = List.of(
                match(Criteria.where("id").is(sessionId)),
                unwind("members", false),
                project().and("members").as("id").andExclude("_id"),
                lookup("userInfo", "id", "userId", "userInfo"),
                addFields().addField("lastSeen").withValue(Map.of("$first", "$userInfo.lastSeen"))
                        .addField("onlineStatus").withValue(Map.of("$first", "$userInfo.onlineStatus")).build(),
                sort(Sort.by(Sort.Direction.DESC, "onlineStatus", "lastSeen")),
                skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );
        List<MemberModel> results = mongoTemplate.aggregate(
                        newAggregation(aggregationOperations)
                                .withOptions(AggregationOptions.builder().allowDiskUse(true).build()),
                        Session.class,
                        MemberModel.class
                )
                .getMappedResults();
        return new PageImpl<>(results, pageable, getMembersCount(sessionId));
    }

    @Override
    public long getMembersCount(String sessionId){
        List<AggregationOperation> aggregationOperations = List.of(
                match(Criteria.where("id").is(sessionId)),
                unwind("members", false),
                count().as("count")
        );
        CountViewModel count = mongoTemplate.aggregate(
                        newAggregation(aggregationOperations)
                                .withOptions(AggregationOptions.builder().allowDiskUse(true).build()),
                        Session.class,
                        CountViewModel.class
                )
                .getUniqueMappedResult();
        return count == null ? 0 : count.getCount();
    }

    @Override
    public long getOnlineMembersCount(String sessionId) {
        List<AggregationOperation> aggregationOperations = List.of(
                match(Criteria.where("id").is(sessionId)),
                unwind("members", false),
                lookup("userInfo", "members", "userId", "userInfo"),
                match(Criteria.where("userInfo.onlineStatus").is(OnlineStatusEnum.ONLINE.name())),
                count().as("count")
        );
        CountViewModel count = mongoTemplate.aggregate(
                        newAggregation(aggregationOperations)
                                .withOptions(AggregationOptions.builder().allowDiskUse(true).build()),
                        Session.class,
                        CountViewModel.class
                )
                .getUniqueMappedResult();
        return count == null ? 0 : count.getCount();
    }

}
