package com.goorm.team9.icontact.sociallogin.security.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class JwtBlacklist {
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void addToBlacklist(String token, long expirationMillis) {
        System.out.println("🛑 [블랙리스트 추가 요청] JWT: " + token + " | 만료 시간(ms): " + expirationMillis);
        blacklistedTokens.put(token, Instant.now().plusMillis(expirationMillis)); // 토큰 만료 시간까지 유지
        System.out.println("✅ [블랙리스트 추가 완료] JWT 블랙리스트 등록됨!");
    }

    public boolean isBlacklisted(String token) {
        Instant expiry = blacklistedTokens.get(token);
        if (expiry == null) {
            System.out.println("✅ [블랙리스트 확인] 해당 토큰은 블랙리스트에 없음");
            return false;
        }

        // 만료된 블랙리스트 토큰 자동 제거
        if (expiry.isBefore(Instant.now())) {
            blacklistedTokens.remove(token);
            System.out.println("✅ [블랙리스트 확인] 해당 토큰이 만료되어 블랙리스트에서 제거됨");
            return false;
        }

        System.out.println("🛑 [블랙리스트 확인] 해당 토큰이 블랙리스트에 존재함");
        return true;
    }
}


