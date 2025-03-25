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
        clientResponseDTO.setCareer(clientEntity.getCareer() != null ? clientEntity.getCareer().getDescription() : null);
        clientResponseDTO.setStatus(clientEntity.getStatus().getDescription());
        clientResponseDTO.setIntroduction(clientEntity.getIntroduction());
        clientResponseDTO.setLink(clientEntity.getLink());
        clientResponseDTO.setProfileImage(clientEntity.getProfileImage());
        clientResponseDTO.setCreatedAt(clientEntity.getCreated_at());
        clientResponseDTO.setUpdatedAt(clientEntity.getUpdated_at());

        if (clientEntity.getIt_topic() != null) {
            clientResponseDTO.setTopic1(clientEntity.getIt_topic().getTopic1().getDescription());
            clientResponseDTO.setTopic2(clientEntity.getIt_topic().getTopic2().getDescription());
            clientResponseDTO.setTopic3(clientEntity.getIt_topic().getTopic3().getDescription());
            clientResponseDTO.setLanguage(clientEntity.getIt_topic().getLanguage().getDescription());
            clientResponseDTO.setFramework(clientEntity.getIt_topic().getFramework().getDescription());
        }

        return clientResponseDTO;
    }
}
