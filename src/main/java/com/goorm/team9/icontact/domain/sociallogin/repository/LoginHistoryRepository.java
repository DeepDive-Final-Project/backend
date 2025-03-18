package com.goorm.team9.icontact.domain.sociallogin.repository;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.sociallogin.entity.LoginHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    // 특정 사용자의 최신 로그인 이력을 조회
    Optional<LoginHistory> findFirstByClientEntityOrderByLoginAtDesc(ClientEntity clientEntity);
}
