package com.goorm.team9.icontact.domain.client.repository;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.sociallogin.entity.OAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByIdAndIsDeletedFalse(Long id);
    List<ClientEntity> findAllByIsDeletedFalse();
    boolean existsByEmail(String email);
    Optional<ClientEntity> findByNickName(String nickName);
    Optional<ClientEntity> findByEmail(String email);
    Optional<ClientEntity> findByProviderAndEmail(String provider, String email);

}