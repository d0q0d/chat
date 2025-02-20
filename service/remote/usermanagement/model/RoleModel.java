package org.tpl.chat.service.remote.usermanagement.model;

import lombok.Data;

@Data
public class RoleModel {

    private Long id;
    private String code;
    private Formation formation;
    private String username;
    private Organization mainOrganization;

    @Data
    public static class Formation {
        private Long id;
        private String title;
        private String code;
    }

    @Data
    public static class Organization {
        private Long id;
        private String name;
        private String code;
    }

}
