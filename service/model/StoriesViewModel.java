package org.tpl.chat.service.model;

import lombok.Data;
import org.tpl.chat.dal.model.Story;

import java.util.List;

@Data
public class StoriesViewModel {

    private String senderId;
    private MemberModel sender;
    private List<Story> stories;
    private Boolean hasUnseen;

}
