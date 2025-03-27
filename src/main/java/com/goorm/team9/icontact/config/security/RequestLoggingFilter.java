package com.goorm.team9.icontact.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("✅ RequestLoggingFilter 도착함");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String host = request.getHeader("host");

        log.info("🌐 요청 도착 → [{}] {}{} | Host: {}", method, uri,
                query != null ? "?" + query : "", host);

        filterChain.doFilter(request, response);
    }
}