package org.tpl.chat.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessPolicyModel implements Serializable {

    private String ownOrganizationCode;
    private String ownOrganizationCodePrefix;
    private List<String> ownFormationCodes = new ArrayList<>();
    private List<String> otherFormationCodes = new ArrayList<>();

}
