package org.tpl.chat.dal.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupAndChannelCountOutputModel {
    private int channelCount;
    private int groupCount;
}
