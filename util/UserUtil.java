package org.tpl.chat.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.socket.WebSocketSession;

@Component
public class UserUtil {
    public String getUserId() {
        try {
            return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getClaim("userId");
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserIdFromSession(WebSocketSession session) {
        return ((Jwt) ((JwtAuthenticationToken) session.getPrincipal()).getPrincipal()).getClaim("userId");
    }

    public String getUserIdFromJwt(Jwt jwt) {
        return jwt.getClaim("userId");
    }

    public String getCurrentToken() {
        return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getTokenValue();
    }

    public Long getCurrentRoleId() {
        try {
            return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getClaim("currentRoleId");
        } catch (Exception e) {
            return null;
        }
    }

    public String getIp(){
        String ip = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            ip = request.getHeader("x-real-ip");
        }catch (Exception ignored){}
        return ip;
    }

}
