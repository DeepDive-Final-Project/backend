package com.goorm.team9.icontact.sociallogin.repository;

import com.goorm.team9.icontact.sociallogin.domain.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
}
