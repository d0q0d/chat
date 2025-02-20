package org.tpl.chat.api.dto;

import lombok.Data;
import org.tpl.chat.dal.model.Story;
import org.tpl.chat.service.model.MemberModel;

import java.util.List;

@Data
public class StoriesViewOutputModel {

    private MemberModel sender;
    private List<StoryOutputModel> stories;
    private Boolean hasUnseen;

}
