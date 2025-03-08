package com.goorm.team9.icontact.sociallogin.security.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JwtBlacklist {
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(JwtBlacklist.class);

    public void addToBlacklist(String token, long expirationMillis) {
        Instant expiryTime = Instant.now().plusMillis(expirationMillis); //토큰 만료 시간까지 유지
        blacklistedTokens.put(token, expiryTime);
        logger.info("🛑 블랙리스트 추가: 토큰={}, 만료시간={}", token, expiryTime);
    }

    public boolean isBlacklisted(String token) {
        Instant expiry = blacklistedTokens.get(token);
        if (expiry == null) {
            return false;
        }

        // 만료된 블랙리스트 토큰 자동 제거
        if (expiry.isBefore(Instant.now())) {
            blacklistedTokens.remove(token);
            logger.info("✅ 만료된 토큰 제거: {}", token);
            return false;
        }
        logger.warn("🛑 블랙리스트에 등록된 토큰 감지: {}", token);
        return true;
    }
}


