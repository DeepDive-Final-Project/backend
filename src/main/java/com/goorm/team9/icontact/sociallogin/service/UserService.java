package com.goorm.team9.icontact.sociallogin.service;

import com.goorm.team9.icontact.sociallogin.domain.User;
import com.goorm.team9.icontact.sociallogin.repository.UserRepository;
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

    private final UserRepository userRepository;

    /**
     * 탈퇴 후 14일이 지나면 재가입 가능 여부 체크
     *
     * @param email 사용자 이메일
     * @return 재가입 가능 여부
     */
    public boolean canReRegister(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.isDeleted()) {
                // 탈퇴 후 14일이 지났는지 확인
                if (user.getDeletedAt() != null &&
                        user.getDeletedAt().plusDays(14).isAfter(LocalDateTime.now())) {
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
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setDeleted(true);
            userRepository.save(user);
        }
    }

    /**
     * 계정 복구 (탈퇴 취소)
     */
    @Transactional
    public void restoreUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setDeleted(false);
            userRepository.save(user);
        }
    }
}
