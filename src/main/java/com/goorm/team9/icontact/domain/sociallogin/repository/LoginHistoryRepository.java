package com.goorm.team9.icontact.domain.sociallogin.repository;

import com.goorm.team9.icontact.domain.sociallogin.domain.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
}
