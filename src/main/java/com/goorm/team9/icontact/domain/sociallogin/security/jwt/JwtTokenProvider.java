package com.goorm.team9.icontact.domain.sociallogin.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
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

    public String createToken(String email, long oauthTokenExpiryMillis,  String provider, String nickname) {

        Date now = new Date();
        long jwtExpiryMillis = Math.min(now.getTime() + validityInMilliseconds, oauthTokenExpiryMillis);
        Date validity = new Date(jwtExpiryMillis);

        return Jwts.builder()
                .setSubject(email)
                .claim("provider", provider)
                .claim("nickname", nickname)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getProvider(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("provider");
    }

    public String getUserEmail(String token) {
        return parseToken(token).getSubject();
    }

    public String getNickname(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("nickname");
    }

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

    public Authentication getAuthentication(String token) {
        String email = getUserEmail(token);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        User principal = new User(email, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    public long getExpirationMillis(String token) {
        return parseToken(token).getExpiration().getTime() - System.currentTimeMillis();
    }

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
