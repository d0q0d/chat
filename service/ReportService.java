package org.tpl.chat.service;

import org.tpl.chat.dal.model.GroupAndChannelCountOutputModel;

import java.time.LocalDateTime;

public interface ReportService {
    GroupAndChannelCountOutputModel getGroupAndChannelCount(String userId);
    GroupAndChannelCountOutputModel getGroupAndChannelCountCreatedByUser(String userId, LocalDateTime from, LocalDateTime to);
    int getP2PCountCreatedByUser(String userId, LocalDateTime from, LocalDateTime to);
}
