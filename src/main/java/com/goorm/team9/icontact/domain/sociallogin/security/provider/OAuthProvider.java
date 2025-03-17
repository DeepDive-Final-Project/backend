/**
 *  getUserInfo 이 부분을 나중에 이 파일로 옮겨주기!!!
 */
package com.goorm.team9.icontact.domain.sociallogin.security.provider;

import java.util.Map;

public interface OAuthProvider {
    String getAccessToken(String code);  // 액세스 토큰을 가져오는 메서드
    Map<String, Object> getUserInfo(String accessToken);  // 유저 정보를 가져오는 메서드
    long getTokenExpiry(String accessToken);
}
