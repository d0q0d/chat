package org.tpl.chat.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class SessionListOutputModel {
  List<SessionOutputModel> sessionList;
}
