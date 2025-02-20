package org.tpl.chat.service.remote.usermanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetTokenRequest {
    private String username;
    private String password;
}

