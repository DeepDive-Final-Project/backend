package com.goorm.team9.icontact.sociallogin.repository;

import com.goorm.team9.icontact.sociallogin.domain.OAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {
    Optional<OAuth> findByProviderAndOauthUserId(String provider, String oauthUserId);
}