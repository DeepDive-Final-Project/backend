package com.goorm.team9.icontact.domain.sociallogin.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * JWT 생성 및 검증을 담당하는 클래스.
 * - JWT 발급
 * - JWT 검증 및 만료 확인
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    private Key key;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("JWT_SECRET 환경 변수가 설정되지 않았습니다.");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    /**
     * JWT 생성 : 사용자 이메일 기반
     */
    public String createToken(String email, long oauthTokenExpiryMillis) {
        Claims claims = Jwts.claims().setSubject(email).build();
        Date now = new Date();
        // JWT 만료 시간 = OAuth Access Token 만료 시간과 기존 만료 시간 중 더 짧은 값 선택
        long jwtExpiryMillis = Math.min(now.getTime() + validityInMilliseconds, oauthTokenExpiryMillis);
        Date validity = new Date(jwtExpiryMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256) // 서명 추가 (보안 강화를 위해 HS256 사용)
                .compact();
    }

    /**
     * JWT에서 사용자 이메일 추출
     */
    public String getUserEmail(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * JWT 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("🛑 JWT 만료됨: {}", e.getMessage());
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            logger.warn("🛑 지원되지 않거나 잘못된 JWT: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("🛑 JWT 값이 비어 있음: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT 기반 사용자 인증 정보 반환
     */
    public Authentication getAuthentication(String token) {
        String email = getUserEmail(token); // 토큰에서 이메일 추출
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER")); // 기본 권한 설정

        User principal = new User(email, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 요청 헤더에서 JWT 추출
     *
     *  @param request HTTP 요청 객체
     *  @return JWT 문자열 (Bearer 제거 후 반환)
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    }

    /**
     * JWT 토큰의 만료 시간 반환
     */
    public long getExpirationMillis(String token) {
        return parseToken(token).getExpiration().getTime() - System.currentTimeMillis();
    }

    /**
     * JWT를 파싱하여 Claims 객체 반환 + 예외 처리
     *
     * @param token JWT 토큰
     * @return Claims (토큰 정보 포함)
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.error("❌ JWT 파싱 실패: {}", token, e);
            throw e;
        }
    }

}
