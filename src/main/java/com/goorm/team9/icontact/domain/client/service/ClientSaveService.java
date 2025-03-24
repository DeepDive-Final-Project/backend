package com.goorm.team9.icontact.domain.client.service;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientSaveService {
    private final ClientRepository clientRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ClientEntity saveClientSafely(ClientEntity clientEntity) {
        String email = clientEntity.getEmail();
        String provider = clientEntity.getProvider().toLowerCase();

        return clientRepository.findByEmailAndProviderNative(email, provider)
                .orElseGet(() -> trySaveAndRetrieve(clientEntity, email, provider));
    }

    private ClientEntity trySaveAndRetrieve(ClientEntity clientEntity, String email, String provider) {
        try {
            clientRepository.save(clientEntity);
            entityManager.flush();
            return clientEntity;
        } catch (DataIntegrityViolationException e) {
            entityManager.clear();
            return retrieveAfterConflict(email, provider);
        }
    }

    private ClientEntity retrieveAfterConflict(String email, String provider) {
        return clientRepository.findByEmailAndProviderNative(email, provider)
                .orElseThrow(() -> new RuntimeException("재조회 실패: 유저 없음"));
    }
}
