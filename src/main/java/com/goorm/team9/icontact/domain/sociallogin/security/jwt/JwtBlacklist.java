package com.goorm.team9.icontact.domain.sociallogin.security.jwt;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nimbusds.jwt.JWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JwtBlacklist {
    private static final Logger logger = LoggerFactory.getLogger(JwtBlacklist.class);

    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public synchronized void addToBlacklist(String token, long expirationMillis) {
        Instant expiryTime = Instant.now().plusMillis(expirationMillis);
        blacklistedTokens.put(token, expiryTime);
        logger.info("🚫 블랙리스트 추가 - 토큰: {}, 만료 시간: {}", token, expiryTime);
    }

    public synchronized boolean isBlacklisted(String token) {
        cleanExpiredTokens();

        boolean isBlacklisted = blacklistedTokens.containsKey(token);
        if (isBlacklisted) {
            logger.warn("🚫 블랙리스트에 등록된 토큰 감지: {}", token);
        }

        return isBlacklisted;
    }

    private void cleanExpiredTokens() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Instant>> iterator = blacklistedTokens.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Instant> entry = iterator.next();
            if (entry.getValue().isBefore(now)) {
                iterator.remove();
                logger.info("✅ 만료된 블랙리스트 토큰 삭제: {}", entry.getKey());
            }
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void removeExpiredTokens() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        logger.info("✅ 블랙리스트에서 만료된 토큰 정리 완료");
    }

}
