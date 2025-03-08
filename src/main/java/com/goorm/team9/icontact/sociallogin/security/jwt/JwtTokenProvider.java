package com.goorm.team9.icontact.sociallogin.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

//    @Value("${jwt.secret}")
//    private String secretKey;
//
//    @Value("${jwt.expiration}")
//    private long validityInMilliseconds;
//
//    private Key key;
//
//    public void init() {
//        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
//    }

    private final String secretKey = "ThisIsAReallyStrongSecretKeyForJwt12345"; // 🚨 256비트 이상으로 설정해야 보안 강화 가능
    private final long validityInMilliseconds = 3600000; // 1시간 (밀리초)
    private final Key key = Keys.hmacShaKeyFor(secretKey.getBytes()); // HMAC 키 생성

    /**
     * JWT 생성
     */
    public String createToken(String email) {
        Claims claims = Jwts.claims().setSubject(email).build();
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

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
     * JWT 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // 만료되거나 변조된 토큰이면 false 반환
        }
    }

    /**
     * JWT에서 인증 정보 추출
     */
    public Authentication getAuthentication(String token) {
        String email = getUserEmail(token); // 토큰에서 이메일 추출
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER")); // 기본 권한 설정

        User principal = new User(email, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * JWT 추출 메서드 추가
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    }

    /**
     * JWT 토큰의 만료 시간을 가져오는 메서드
     */
    public long getExpirationMillis(String token) {
        return parseToken(token).getExpiration().getTime() - System.currentTimeMillis();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
