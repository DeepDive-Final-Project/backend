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
    public boolean canReRegister(String email, String provider) {
        Optional<ClientEntity> clientOptional = clientRepository.findByEmailAndProviderAndIsDeletedTrue(email, provider);
        if (clientOptional.isPresent()) {
            ClientEntity clientEntity = clientOptional.get();
            if (clientEntity.getDeleted_at() != null) {
                // 14일이 지났으면 DB에서 삭제
                if (clientEntity.getDeleted_at().plusDays(14).isBefore(LocalDateTime.now())) {
                    clientRepository.delete(clientEntity); // 완전 삭제
                    return true; // 삭제했으니 재가입 가능
                } else {
                    return false; // 아직 14일 안 지남 → 재가입 불가
                }
            }
        }
        return true; // 탈퇴 이력이 없으면 재가입 가능
    }

    /**
     * 회원 탈퇴 처리 (소프트 삭제 적용)
     */
    @Transactional
    public void deleteUserByEmail(String email, String provider) {
        Optional<ClientEntity> clientOptional = clientRepository.findByEmailAndProviderAndIsDeletedFalse(email, provider);
        if (clientOptional.isPresent()) {
            ClientEntity clientEntity = clientOptional.get();
            clientEntity.setDeleted(true);
            clientEntity.setDeleted_at(LocalDateTime.now());
            clientRepository.save(clientEntity);
        }
    }

    /**
     * 계정 복구 (탈퇴 취소)
     */
    @Transactional
    public void restoreUser(String email, String provider) {
        Optional<ClientEntity> clientOptional = clientRepository.findByEmailAndProviderAndIsDeletedTrue(email, provider);
        if (clientOptional.isPresent()) {
            ClientEntity clientEntity = clientOptional.get();
            clientEntity.setDeleted(false);
            clientEntity.setDeleted_at(null);
            clientRepository.save(clientEntity);
        }
    }
}
