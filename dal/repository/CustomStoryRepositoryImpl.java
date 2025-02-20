package org.tpl.chat.dal.repository;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.Story;
import org.tpl.chat.dal.repository.view.CountViewModel;
import org.tpl.chat.service.model.AccessPolicyModel;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.service.model.StoriesViewModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@RequiredArgsConstructor
public class CustomStoryRepositoryImpl implements CustomStoryRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public void seenStory(String storyId, String userId) {
        var query = new Query(Criteria.where("id").is(storyId).and("senderId").ne(userId));
        var update = new Update().addToSet("receiverIds", userId);
        mongoTemplate.updateMulti(query, update, Story.class);
    }

    @Override
    public Page<MemberModel> getReceivers(String storyId, Pageable pageable) {
        List<AggregationOperation> aggregationOperations = List.of(
                match(Criteria.where("id").is(storyId)),
                unwind("receiverIds", false),
                project().and("receiverIds").as("id").andExclude("_id"),
                sort(Sort.by(Sort.Direction.ASC, "id")),
                skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );
        List<MemberModel> results = mongoTemplate.aggregate(
                        newAggregation(aggregationOperations)
                                .withOptions(AggregationOptions.builder().allowDiskUse(true).build()),
                        Story.class,
                        MemberModel.class
                )
                .getMappedResults();
        Query query = new Query(Criteria.where("id").is(storyId));
        Story story = mongoTemplate.findOne(query, Story.class);
        long total = (story == null || story.getReceiverIds() == null) ? 0 : story.getReceiverIds().size();
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<StoriesViewModel> getStoriesBasedOnRole(String userId, Pageable pageable, AccessPolicyModel accessPolicyModel, LocalDateTime from, LocalDateTime to) {
        List<AggregationOperation> baseAggregation = getBaseAggregationForGettingStoriesView(userId, accessPolicyModel, from, to);
        List<AggregationOperation> resultAggregation = new ArrayList<>(baseAggregation);
        resultAggregation.add(skip((long) pageable.getPageNumber() * pageable.getPageSize()));
        resultAggregation.add(limit(pageable.getPageSize()));
        List<StoriesViewModel> results = mongoTemplate.aggregate(
                        newAggregation(resultAggregation)
                                .withOptions(AggregationOptions.builder().allowDiskUse(true).build()),
                        Story.class,
                        StoriesViewModel.class
                )
                .getMappedResults();
        List<AggregationOperation> countAggregation = new ArrayList<>(baseAggregation);
        countAggregation.add(count().as("count"));
        CountViewModel count = mongoTemplate.aggregate(
                        newAggregation(countAggregation)
                                .withOptions(AggregationOptions.builder().allowDiskUse(true).build()),
                        Story.class,
                        CountViewModel.class
                )
                .getUniqueMappedResult();
        return new PageImpl<>(results, pageable, count == null ? 0 : count.getCount());
    }

    private List<AggregationOperation> getBaseAggregationForGettingStoriesView(String userId, AccessPolicyModel accessPolicyModel, LocalDateTime from, LocalDateTime to){
        return List.of(
                match(getStoriesViewModelCriteria(userId, accessPolicyModel, from, to)),
                addFields().addField("seen").withValue(Map.of(
                        "$cond", List.of(
                                Map.of("$and", List.of(Map.of("$isArray", "$receiverIds"), Map.of("$in", List.of(userId, "$receiverIds")))),
                                true,
                                false
                        ))).build(),
                project().andExclude("receiverIds"),
                sort(Sort.by(Sort.Direction.DESC, "createdDate")),
                group("senderId")
                        .addToSet("seen").as("seen")
                        .first("senderId").as("senderId")
                        .last("createdDate").as("lastCreatedDate")
                        .push("$$ROOT").as("stories"),
                addFields().addField("hasUnseen").withValue(Map.of("$anyElementTrue", Map.of("$map", Map.of("input", "$seen", "in", Map.of("$eq", List.of("$$this", false)))))).build(),
                sort(Sort.by(Sort.Direction.DESC,"hasUnseen", "lastCreatedDate"))
        );
    }

    private Criteria getStoriesViewModelCriteria(String userId, AccessPolicyModel accessPolicyModel, LocalDateTime from, LocalDateTime to) {
        return Criteria.where("createdDate").gt(from).lt(to)
                .and("senderId").ne(userId)
                .orOperator(
                        new Criteria().andOperator(Criteria.where("senderOrganizationCode").regex('^' + accessPolicyModel.getOwnOrganizationCodePrefix()), Criteria.where("senderFormationCode").in(accessPolicyModel.getOwnFormationCodes())),
                        Criteria.where("senderFormationCode").in(accessPolicyModel.getOtherFormationCodes())
                );
    }

}
