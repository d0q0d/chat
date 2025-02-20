package org.tpl.chat.service.remote.usermanagement;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.CacheToken;
import org.tpl.chat.service.model.AccessPolicyModel;
import org.tpl.chat.service.remote.cachetoken.CacheTokenService;
import org.tpl.chat.service.remote.model.UserManagementProfile;
import org.tpl.chat.service.remote.usermanagement.model.GetTokenRequest;
import org.tpl.chat.service.remote.usermanagement.model.OAuth2AccessTokenResponseModel;
import org.tpl.chat.service.remote.usermanagement.model.RefreshTokenRequest;
import org.tpl.chat.service.remote.usermanagement.model.RoleModel;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.service.exception.NotFoundException;
import org.tpl.util.common.service.exception.UnauthorizedException;
import org.tpl.util.common.service.remote.FeignGeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UsermanagementApiAdapter {

    private final UsermanagementApiClient authApiClient;
    private final CacheTokenService cacheTokenService;
    private final UserUtil userUtil;
    @Value("${security.oauth2.user.username}")
    private String username;
    @Value("${security.oauth2.user.password}")
    private String password;
    private static final String ACCESS_TOKEN_KEY = "usermenagment:access";
    private static final String REFRESH_TOKEN_KEY = "usermenagment:refresh";

    public String getToken() {
        return cacheTokenService.getById(ACCESS_TOKEN_KEY)
                .map(CacheToken::getToken)
                .orElseGet(() -> callAccessTokenApiAndSaveTokens().getAccessToken());
    }

    public void refreshToken() {
        cacheTokenService.getById(REFRESH_TOKEN_KEY)
                .ifPresentOrElse(
                        cacheToken -> callRefreshTokenApiAndSaveTokens(cacheToken.getToken()),
                        this::callAccessTokenApiAndSaveTokens
                );
    }

    @Retryable(retryFor = {UnauthorizedException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 100))
    public UserManagementProfile getProfile(String userId) {
        UserManagementProfile profile;
        String token = getToken();
        try {
            profile = authApiClient.getProfile("Bearer "+ token, userId);
        }catch (FeignGeneralException e){
            if (e.getStatus() == 401){
                refreshToken();
                throw new UnauthorizedException();
            }
            if (e.getStatus() == 403) {
                refreshToken();
                throw new UnauthorizedException();
            }
            if (e.getStatus() == 404){
                throw new NotFoundException();
            }
            throw e;
        }
        return profile;
    }

    @Retryable(retryFor = {UnauthorizedException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 100))
    public List<UserManagementProfile> getProfilesByList(Set<String> ids) {
        List<UserManagementProfile> profiles;
        String token = getToken();
        try {
            profiles = authApiClient.getProfilesByList("Bearer "+ token, ids);
        }catch (FeignGeneralException e){
            if (e.getStatus() == 401){
                refreshToken();
                throw new UnauthorizedException();
            }
            if (e.getStatus() == 403) {
                refreshToken();
                throw new UnauthorizedException();
            }
            throw e;
        }
        return profiles;
    }

    public Boolean hasAccessBasedOnRole(String userId){
        Boolean hasAccess;
        String token = userUtil.getCurrentToken();
        try {
            hasAccess = authApiClient.hasAccessBasedOnRole("Bearer "+ token, userId);
        }catch (FeignGeneralException e){
            if (e.getStatus() == 401){
                refreshToken();
                throw new UnauthorizedException();
            }
            if (e.getStatus() == 403) {
                refreshToken();
                throw new UnauthorizedException();
            }
            throw e;
        }
        return hasAccess;
    }

    @Retryable(retryFor = {UnauthorizedException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 100))
    public RoleModel getRoleById(Long id) {
        RoleModel roleModel;
        String token = getToken();
        try {
            roleModel = authApiClient.getRoleById("Bearer " + token, id);
        } catch (FeignGeneralException e) {
            if (e.getStatus() == 401) {
                refreshToken();
                throw new UnauthorizedException();
            }
            if (e.getStatus() == 403) {
                refreshToken();
                throw new UnauthorizedException();
            }
            if (e.getStatus() == 404) {
                throw new IllegalStateException("role not found.");
            }
            throw e;
        }
        return roleModel;
    }

    @Retryable(retryFor = {UnauthorizedException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 100))
    @Cacheable(value = "accessPolicyCache", key = "'roleId=' + #roleId")
    public AccessPolicyModel getAccessPolicyModelByRoleId(Long roleId) {
        AccessPolicyModel accessPolicyModel;
        String token = getToken();
        try {
            accessPolicyModel = authApiClient.getAccessPolicyModelByRoleId("Bearer " + token, roleId);
        } catch (FeignGeneralException e) {
            if (e.getStatus() == 401) {
                refreshToken();
                throw new UnauthorizedException();
            }
            if (e.getStatus() == 403) {
                refreshToken();
                throw new UnauthorizedException();
            }
            if (e.getStatus() == 404) {
                throw new IllegalStateException("role not found.");
            }
            throw e;
        }
        return accessPolicyModel;
    }

    private OAuth2AccessTokenResponseModel callAccessTokenApiAndSaveTokens() {
        OAuth2AccessTokenResponseModel responseModel;
        try {
            responseModel = authApiClient.getToken(new GetTokenRequest(username, password));
        } catch (FeignGeneralException e) {
            throw e;
        }
        saveAccessAccessTokenAndRefreshToken(responseModel);
        return responseModel;
    }

    private void callRefreshTokenApiAndSaveTokens(String refreshToken) {
        OAuth2AccessTokenResponseModel responseModel;
        try {
            responseModel = authApiClient.refreshToken(new RefreshTokenRequest(refreshToken));
        } catch (FeignGeneralException e) {
            if (e.getStatus() == 400){
                callAccessTokenApiAndSaveTokens();
                return;
            }
            throw e;
        }
        saveAccessAccessTokenAndRefreshToken(responseModel);
    }

    private void saveAccessAccessTokenAndRefreshToken(OAuth2AccessTokenResponseModel accessTokenResponseModel) {
        cacheTokenService.save(new CacheToken(ACCESS_TOKEN_KEY, accessTokenResponseModel.getAccessToken()));
        cacheTokenService.save(new CacheToken(REFRESH_TOKEN_KEY, accessTokenResponseModel.getRefreshToken()));
    }

}
