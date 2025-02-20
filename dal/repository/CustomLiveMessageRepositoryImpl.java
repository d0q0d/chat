package org.tpl.chat.dal.repository;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.LiveMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class CustomLiveMessageRepositoryImpl implements CustomLiveMessageRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final LiveMessageRepository repository;
    private static final String SPLITTER = ":";
    private static final String NAMESPACE = "liveMessage";

    @Override
    public List<LiveMessage> findAllByTopic(String topic) {
        Set<String> keys = redisTemplate.keys(NAMESPACE + SPLITTER + topic + SPLITTER + "*");
        if (CollectionUtils.isEmpty(keys)) return new ArrayList<>();
        List<LiveMessage> results = new ArrayList<>();
        for (String key : keys) {
            key = key.substring((NAMESPACE + SPLITTER).length());
            Optional<LiveMessage> opt = repository.findById(key);
            opt.ifPresent(results::add);
        }
        return results;
    }

    @Override
    public void deleteById(String id) {
        Set<String> keys = redisTemplate.keys(NAMESPACE + SPLITTER + "*" + SPLITTER + id);
        if (CollectionUtils.isEmpty(keys)) return;
        redisTemplate.delete(keys);
    }

}
