package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ClientRepository clientRepository;

    public boolean canReRegister(String email, String provider) {
        Optional<ClientEntity> clientOptional = clientRepository.findByEmailAndProviderAndIsDeletedTrue(email, provider);
        if (clientOptional.isPresent()) {
            ClientEntity clientEntity = clientOptional.get();
            if (clientEntity.getDeleted_at() != null) {
                if (clientEntity.getDeleted_at().plusDays(14).isBefore(LocalDateTime.now())) {
                    clientRepository.delete(clientEntity);
                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

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
