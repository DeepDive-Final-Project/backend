package com.goorm.team9.icontact.config.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;
import org.springframework.web.util.WebUtils;

import java.util.Base64;

public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    private final boolean secure;
    private final String domain;

    public HttpCookieOAuth2AuthorizationRequestRepository(boolean secure, String domain) {
        this.secure = secure;
        this.domain = domain;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        if (cookie != null) {
            return deserialize(cookie.getValue());
        }
        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }
        String serialized = serialize(authorizationRequest);
        addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized, COOKIE_EXPIRE_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return authorizationRequest;
    }

    private void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(secure);
        response.addCookie(cookie);
        if (!domain.isEmpty()) cookie.setDomain(domain);
    }

    private String serialize(OAuth2AuthorizationRequest object) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
    }

    private OAuth2AuthorizationRequest deserialize(String cookie) {
        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie));
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secure);

        if (!domain.isEmpty()) {
            cookie.setDomain(domain);
        }

        if (secure) {
            response.addHeader("Set-Cookie", String.format(
                    "%s=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None%s",
                    name, value, maxAge,
                    domain.isEmpty() ? "" : "; Domain=" + domain
            ));
        } else {
            response.addCookie(cookie);
        }
    }

}