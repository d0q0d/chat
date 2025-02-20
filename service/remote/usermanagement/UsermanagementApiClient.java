package org.tpl.chat.service.remote.usermanagement;

import org.tpl.chat.service.model.AccessPolicyModel;
import org.tpl.chat.service.remote.usermanagement.model.RoleModel;
import org.tpl.chat.service.remote.model.UserManagementProfile;
import org.tpl.chat.service.remote.usermanagement.model.GetTokenRequest;
import org.tpl.chat.service.remote.usermanagement.model.OAuth2AccessTokenResponseModel;
import org.tpl.chat.service.remote.usermanagement.model.RefreshTokenRequest;
import org.tpl.util.common.service.remote.DefaultFeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@FeignClient(name = "${usermanagement.service.name}", url = "${usermanagement.base.url}", configuration = DefaultFeignErrorDecoder.class)
public interface UsermanagementApiClient {

    @PostMapping(path = "/public/v1/usermanagement/tokens/generate/password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, headers = "Authorization=" + "${usermanagement.basic.auth}")
    OAuth2AccessTokenResponseModel getToken(GetTokenRequest getTokenRequest);

    @PostMapping(path = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, headers = "Authorization=" + "${usermanagement.basic.auth}")
    OAuth2AccessTokenResponseModel refreshToken(RefreshTokenRequest refreshTokenRequest);

    @GetMapping(path = "/api/v1/usermanagement/users/profile/{id}")
    UserManagementProfile getProfile(@RequestHeader("Authorization") String token, @PathVariable("id") String userId);

    @GetMapping(path = "/api/v1/usermanagement/users/profile/by-list")
    List<UserManagementProfile> getProfilesByList(@RequestHeader("Authorization") String token, @RequestParam Set<String> ids);

    @GetMapping("/api/v1/usermanagement/users/access-to-user/based-on-role/{userId}")
    Boolean hasAccessBasedOnRole(@RequestHeader("Authorization") String token, @PathVariable("userId") String userId);

    @GetMapping("/api/v1/usermanagement/roles/{id}")
    RoleModel getRoleById(@RequestHeader("Authorization") String token, @PathVariable("id") Long id);

    @GetMapping("/api/v1/usermanagement/access-policy")
    AccessPolicyModel getAccessPolicyModelByRoleId(@RequestHeader("Authorization") String token, @RequestParam Long roleId);
}
