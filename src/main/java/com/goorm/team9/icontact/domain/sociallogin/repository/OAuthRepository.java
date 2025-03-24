package com.goorm.team9.icontact.domain.sociallogin.repository;

import com.goorm.team9.icontact.domain.sociallogin.entity.OAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {
    Optional<OAuth> findByProviderAndOauthUserId(String provider, String oauthUserId);
    Optional<OAuth> findByEmail(String email);
    Optional<OAuth> findByRefreshToken(String refreshToken);
    Optional<OAuth> findByProviderAndEmail(String provider, String email);
}