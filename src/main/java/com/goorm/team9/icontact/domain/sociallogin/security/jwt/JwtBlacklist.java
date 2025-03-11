package com.goorm.team9.icontact.domain.sociallogin.security.jwt;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * JWT 블랙리스트 관리 클래스.
 * - 로그아웃된 JWT를 블랙리스트에 추가하여 재사용 방지.
 * - 만료된 토큰은 자동 삭제하여 메모리 최적화.
 */
@Component
public class JwtBlacklist {
    private static final Logger logger = LoggerFactory.getLogger(JwtBlacklist.class);

    /**
     * 블랙리스트에 등록된 JWT와 만료 시간을 저장하는 맵.
     * - Key: JWT 토큰 값
     * - Value: 만료 시간 (`Instant`)
     */
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * JWT를 블랙리스트에 추가.
     *
     * @param token 블랙리스트에 추가할 JWT
     * @param expirationMillis 토큰의 만료 시간 (밀리초)
     */
    public synchronized void addToBlacklist(String token, long expirationMillis) {
        Instant expiryTime = Instant.now().plusMillis(expirationMillis);
        blacklistedTokens.put(token, expiryTime);
        logger.info("🚫 블랙리스트 추가 - 토큰: {}, 만료 시간: {}", token, expiryTime);
    }

    /**
     * JWT가 블랙리스트에 등록되어 있는지 확인.
     *
     * @param token 확인할 JWT
     * @return 블랙리스트 여부 (true = 차단됨)
     */
    public synchronized boolean isBlacklisted(String token) {
        cleanExpiredTokens();

        if (blacklistedTokens.containsKey(token)) {
            logger.info("🛑 블랙리스트에 등록된 토큰 감지: {}", token);
            return true;
        }
        return false;
    }

    /**
     * 만료된 블랙리스트 토큰 자동 제거.
     */
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

//    /**
//     * 10분마다 블랙리스트에서 만료된 토큰을 제거하는 스케줄러.
//     */
//    @Scheduled(fixedRate = 600_000) // 10분마다 실행
//    public void cleanExpiredTokens() {
//        Instant now = Instant.now();
//        Iterator<Instant> iterator = blacklistedTokens.keySet().iterator();
//
//        while (iterator.hasNext()) {
//            Instant expiryTime = iterator.next();
//            if (expiryTime.isBefore(now)) {
//                iterator.remove();
//            } else {
//                break; // 이후 토큰들은 아직 유효하므로 종료
//            }
//        }
//
//        logger.info("♻️ 만료된 JWT 블랙리스트 정리 완료");
//    }
}
