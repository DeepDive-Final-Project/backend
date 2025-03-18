package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.sociallogin.entity.LoginHistory;
import com.goorm.team9.icontact.domain.sociallogin.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    /**
     * 사용자의 로그인 이력을 저장
     */
    @Transactional
    public void saveLoginHistory(ClientEntity clientEntity, String provider) {
        LoginHistory loginHistory = LoginHistory.builder()
                .clientEntity(clientEntity)
                .provider(provider)
                .loginAt(LocalDateTime.now())
                .build();
        loginHistoryRepository.save(loginHistory);
    }

    /**
     * 사용자의 최신 로그인 제공자 조회
     */
    public Optional<String> getLastLoginProvider(ClientEntity clientEntity) {
        return loginHistoryRepository.findFirstByClientEntityOrderByLoginAtDesc(clientEntity)
                .map(LoginHistory::getProvider);
    }
}
