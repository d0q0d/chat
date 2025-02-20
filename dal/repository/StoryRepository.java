package org.tpl.chat.dal.repository;

import org.tpl.chat.dal.model.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;

public interface StoryRepository extends MongoRepository<Story, String> , CustomStoryRepository{

    long countBySenderIdAndCreatedDateBetween(String senderId, LocalDateTime from, LocalDateTime to);
    @Query(fields = "{receiverIds:0}")
    Page<Story> findAllBySenderIdAndCreatedDateBetween(Pageable pageable, String senderId, LocalDateTime from, LocalDateTime to);
    @Query(fields = "{receiverIds:0}")
    Page<Story> findAllBySenderIdAndCreatedDateBefore(Pageable pageable, String senderId, LocalDateTime dateTime);
    @Query(fields = "{receiverIds:0}")
    Page<Story> findAllBySenderId(Pageable pageable, String senderId);

}
