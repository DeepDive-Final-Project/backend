package com.goorm.team9.icontact.sociallogin.repository;

import com.goorm.team9.icontact.sociallogin.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
    Optional<User> findByEmail(String email);
}