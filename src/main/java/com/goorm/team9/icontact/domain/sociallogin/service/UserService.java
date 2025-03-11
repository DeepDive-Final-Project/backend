package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 사용자 관리 서비스.
 * - 회원 탈퇴 (소프트 삭제)
 * - 탈퇴 후 14일 후 재가입 가능 여부 체크
 * - 계정 복구
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final ClientRepository clientRepository;

    /**
     * 탈퇴 후 14일이 지나면 재가입 가능 여부 체크
     *
     * @param email 사용자 이메일
     * @return 재가입 가능 여부
     */
    public boolean canReRegister(String email) {
        Optional<ClientEntity> clientOptional = clientRepository.findByEmail(email);
        if (clientOptional.isPresent()) {
            ClientEntity clientEntity = clientOptional.get();
            if (clientEntity.isDeleted()) {
                // 탈퇴 후 14일이 지났는지 확인
                if (clientEntity.getDeleted_at() != null &&
                        clientEntity.getDeleted_at().plusDays(14).isAfter(LocalDateTime.now())) {
                    return false; // 14일이 지나지 않음 -> 재가입 불가능
                }
            }
        }
        return true; // 탈퇴 이력이 없거나 14일이 지남 -> 재가입 가능
    }

    /**
     * 회원 탈퇴 처리 (소프트 삭제 적용)
     */
    @Transactional
    public void deleteUserByEmail(String email) {
        Optional<ClientEntity> clientOptional = clientRepository.findByEmail(email);
        if (clientOptional.isPresent()) {
            ClientEntity clientEntity = clientOptional.get();
            clientEntity.setDeleted(true);
            clientRepository.save(clientEntity);
        }
    }

    /**
     * 계정 복구 (탈퇴 취소)
     */
    @Transactional
    public void restoreUser(String email) {
        Optional<ClientEntity> clientOptional = clientRepository.findByEmail(email);
        if (clientOptional.isPresent()) {
            ClientEntity clientEntity = clientOptional.get();
            clientEntity.setDeleted(false);
            clientRepository.save(clientEntity);
        }
    }
}
