package com.goorm.team9.icontact.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Configuration
public class OAuth2Config {

    @Value("${app.domain:}") // application.yml에서 domain 받아오기 (없으면 빈 문자열)
    private String domain;

    @Value("${app.oauth.cookie-secure:true}") // 필요하다면 secure도 외부 설정 가능
    private boolean secure;

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository(secure, domain);
    }
}

