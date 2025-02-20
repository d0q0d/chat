package org.tpl.chat.service;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.GroupAndChannelCountOutputModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SessionService sessionService;

    @Override
    public GroupAndChannelCountOutputModel getGroupAndChannelCount(String userId) {
        return sessionService.getGroupAndChannelCount(userId);
    }

    @Override
    public GroupAndChannelCountOutputModel getGroupAndChannelCountCreatedByUser(String userId, LocalDateTime from, LocalDateTime to) {
        return sessionService.getGroupAndChannelCountCreatedByUser(userId, from, to);
    }

    @Override
    public int getP2PCountCreatedByUser(String userId, LocalDateTime from, LocalDateTime to) {
        return sessionService.getP2PCountCreatedByUser(userId, from, to);
    }
}
