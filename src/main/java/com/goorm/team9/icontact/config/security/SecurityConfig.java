package com.goorm.team9.icontact.config.security;

import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.sociallogin.security.jwt.*;
import com.goorm.team9.icontact.domain.sociallogin.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklist jwtBlacklist;
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthorizationRequestRepository;
    private final ClientRepository clientRepository;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/ws-chat/**",
                "/actuator/**",
                "/swagger-ui/**",
                "/v3/api-docs/**"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("✅ SecurityFilterChain is active");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 분리
                // .addFilterBefore(new OAuth2DomainRedirectFilter(), CorsFilter.class)
                // .addFilterBefore(new RequestLoggingFilter(), CorsFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/ws-chat/**",
                                "/topic/**",
                                "/app/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/api/**",
                                "/v3/**",
                                "/api/v3/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/actuator/**",
                                "/actuator/health",
                                "/actuator/prometheus",
                                "/api/auth/**",
                                "/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/auth/logout",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/auth/restore").hasAuthority("ROLE_WITHDRAWN")
                        .requestMatchers("/auth/home").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization") // 프론트에서 요청할 경로
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*")
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(new JwtAuthenticationSuccessHandler(jwtTokenProvider, clientRepository)) // JWT 발급 후 반환
                        .successHandler(new JwtAuthenticationSuccessHandler(jwtTokenProvider, clientRepository)) // JWT 발급 후 반환
                        .failureHandler((request, response, exception) -> {
                            log.error("❌ OAuth2 인증 실패: {}", exception.getMessage(), exception); // 실패 원인 로그 출력
                            response.sendRedirect("/login?error=" + exception.getMessage()); // 에러 메시지 포함
                        })

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
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), // 보호된 API 접근 시 401 응답
                                request -> request.getRequestURI().startsWith("/auth/home") // 여기만 401
                        )
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://3.34.165.63:3000",
                "http://43.201.245.222:3000",
                "http://localhost:8080",
                "http://3.34.165.63:8080",
                "http://43.201.245.222:8080",
                "https://www.i-contacts.link",
                "http://localhost:5173"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "FETCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L); // 프리플라이트 요청 캐싱 (1시간)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(new ForwardedHeaderFilter());
        filterRegBean.setOrder(0); // 제일 먼저 적용되도록
        return filterRegBean;
    }

    @Bean
    public FilterRegistrationBean<OAuth2DomainRedirectFilter> oauth2DomainRedirectFilter() {
        FilterRegistrationBean<OAuth2DomainRedirectFilter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(new OAuth2DomainRedirectFilter());
        filterRegBean.setOrder(1); // ForwardedHeaderFilter보다 뒤에서 실행
        return filterRegBean;
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
    FilterRegistrationBean<RequestLoggingFilter> filterRegBean = new FilterRegistrationBean<>();
    filterRegBean.setFilter(new RequestLoggingFilter());
    filterRegBean.setOrder(0); // 가장 먼저 실행되게
    return filterRegBean;
    }
    
}