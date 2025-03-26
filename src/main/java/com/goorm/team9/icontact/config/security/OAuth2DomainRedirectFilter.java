package com.goorm.team9.icontact.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class OAuth2DomainRedirectFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String host = request.getServerName(); // 도메인 (ex. www.i-contacts.link)
        String uri = request.getRequestURI(); // 요청 경로 (ex. /oauth2/authorization/kakao)

        // www 도메인에서 /oauth2/authorization/... 요청이 들어오면 리디렉션
        if (host.equals("www.i-contacts.link") && uri.startsWith("/oauth2/authorization/")) {
            String queryString = request.getQueryString();
            String redirectUrl = "https://api.i-contacts.link/" + uri;
            if (queryString != null) {
                redirectUrl += "?" + queryString;
            }
            response.sendRedirect(redirectUrl);
            return;
        }

        filterChain.doFilter(request, response);
    }
}