package com.goorm.team9.icontact.domain.client.converter;

import com.goorm.team9.icontact.domain.client.dto.response.ClientResponseDTO;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import org.springframework.stereotype.Component;

@Component
public class ClientConverter {

    public ClientResponseDTO toResponseDTO(ClientEntity clientEntity) {
        ClientResponseDTO clientResponseDTO = new ClientResponseDTO();
        clientResponseDTO.setId(clientEntity.getId());
        clientResponseDTO.setNickName(clientEntity.getNickName());
        clientResponseDTO.setEmail(clientEntity.getEmail());
        clientResponseDTO.setRole(clientEntity.getRole().getDescription());
        clientResponseDTO.setCareer(clientEntity.getCareer().getDescription());
        clientResponseDTO.setStatus(clientEntity.getStatus().getDescription());
        clientResponseDTO.setIntroduction(clientEntity.getIntroduction());
        clientResponseDTO.setLink(clientEntity.getLink());
        clientResponseDTO.setProfileImage(clientEntity.getProfileImage());
        clientResponseDTO.setChatOpportunity(clientEntity.getChatOpportunity());
        clientResponseDTO.setChatMessage(clientEntity.getChatMessage());
        clientResponseDTO.setOffline(clientEntity.isOffline());
        clientResponseDTO.setCreatedAt(clientEntity.getCreated_at());
        clientResponseDTO.setUpdatedAt(clientEntity.getUpdated_at());
        return clientResponseDTO;
    }
}
