package org.tpl.chat.dal.model;

import lombok.Data;

@Data
public class MessageMetaData {
    private String previewUrl;
    private int length;
    private long size;
}
