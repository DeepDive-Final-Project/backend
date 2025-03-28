package com.goorm.team9.icontact.domain.client.repository;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByIdAndIsDeletedFalse(Long id);
    List<ClientEntity> findAllByIsDeletedFalse();
    boolean existsByEmail(String email);
    boolean existsByEmailAndProvider(String email, String provider);
    Optional<ClientEntity> findByNickName(String nickName);
    Optional<ClientEntity> findByEmail(String email);

    @Query(value = "SELECT * FROM client WHERE email = :email AND provider = :provider", nativeQuery = true)
    Optional<ClientEntity> findByEmailAndProviderNative(@Param("email") String email, @Param("provider") String provider);

    Optional<ClientEntity> findByEmailAndProviderAndIsDeletedFalse(String email, String provider);
    Optional<ClientEntity> findByEmailAndProviderAndIsDeletedTrue(String email, String provider);
    boolean existsByEmailAndProviderAndIsDeletedFalse(String email, String provider);
    Optional<ClientEntity> findByEmailAndProvider(String email, String provider);
}