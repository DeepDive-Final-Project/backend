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

    @Transactional
    public void saveLoginHistory(ClientEntity clientEntity, String provider) {
        LoginHistory loginHistory = LoginHistory.builder()
                .clientEntity(clientEntity)
                .provider(provider)
                .loginAt(LocalDateTime.now())
                .build();
        loginHistoryRepository.save(loginHistory);
    }

    public Optional<String> getLastLoginProvider(ClientEntity clientEntity) {
        return loginHistoryRepository.findFirstByClientEntityOrderByLoginAtDesc(clientEntity)
                .map(LoginHistory::getProvider);
    }

}
