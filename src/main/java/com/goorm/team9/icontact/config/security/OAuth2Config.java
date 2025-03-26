package com.goorm.team9.icontact.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Configuration
public class OAuth2Config {

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthorizationRequestRepository(
            @Value("${app.oauth.cookie-secure}") boolean secure
    ) {
        return new HttpCookieOAuth2AuthorizationRequestRepository(secure);
    }
}

