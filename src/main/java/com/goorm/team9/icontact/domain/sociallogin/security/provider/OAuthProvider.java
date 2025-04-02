package com.goorm.team9.icontact.domain.sociallogin.security.provider;

import java.util.Map;

public interface OAuthProvider {

    String getAccessToken(String code);
    Map<String, Object> getUserInfo(String accessToken);
    long getTokenExpiry(String accessToken);

}
