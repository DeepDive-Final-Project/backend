package com.goorm.team9.icontact.sociallogin.config;

import com.goorm.team9.icontact.sociallogin.security.jwt.JwtAuthenticationFilter;
import com.goorm.team9.icontact.sociallogin.security.jwt.JwtAuthenticationSuccessHandler;
import com.goorm.team9.icontact.sociallogin.security.jwt.JwtBlacklist;
import com.goorm.team9.icontact.sociallogin.security.jwt.JwtLogoutSuccessHandler;
import com.goorm.team9.icontact.sociallogin.security.jwt.JwtTokenProvider;
import com.goorm.team9.icontact.sociallogin.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklist jwtBlacklist;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                                "/oauth2/**", "/login/**", "/auth/logout" //로그인/로그아웃 관련 엔드포인트는 예외 처리
                        ).permitAll()                        .requestMatchers("/auth/home").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/github")
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(new JwtAuthenticationSuccessHandler(jwtTokenProvider)) // JWT 발급 후 반환
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, jwtBlacklist),
                        UsernamePasswordAuthenticationFilter.class) // JWT 필터 적용

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")  // 로그아웃 엔드포인트
                        .logoutSuccessHandler(jwtLogoutSuccessHandler) // 로그아웃 후 accessToken 삭제!
                        .invalidateHttpSession(true)  // 세션 무효화
                        .clearAuthentication(true)    // 인증 정보 삭제
                        .deleteCookies("JSESSIONID", "Authorization") // 쿠키 삭제
                )

                // 인증되지 않은 경우, 보호된 API만 401 응답
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), // 보호된 API 접근 시 401 응답
                                request -> request.getRequestURI().startsWith("/auth/home") // 여기만 401
                        )
                );
        return http.build();
    }
}