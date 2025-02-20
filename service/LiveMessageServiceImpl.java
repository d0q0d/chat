package org.tpl.chat.service;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.LiveMessage;
import org.tpl.chat.dal.repository.CustomLiveMessageRepository;
import org.tpl.chat.dal.repository.LiveMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiveMessageServiceImpl implements LiveMessageService {

    private final LiveMessageRepository repository;
    private final CustomLiveMessageRepository customRepository;

    @Override
    public LiveMessage save(LiveMessage liveMessage) {
        return repository.save(liveMessage);
    }

    @Override
    public List<LiveMessage> getAllByTopic(String topic) {
        return customRepository.findAllByTopic(topic);
    }

    @Override
    public void deleteById(String id) {
        customRepository.deleteById(id);
    }

}
