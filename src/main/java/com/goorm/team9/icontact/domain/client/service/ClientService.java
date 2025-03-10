package com.goorm.team9.icontact.domain.client.service;

import com.goorm.team9.icontact.common.error.ClientErrorCode;
import com.goorm.team9.icontact.common.exception.CustomException;
import com.goorm.team9.icontact.domain.client.converter.ClientConverter;
import com.goorm.team9.icontact.domain.client.dto.response.ClientResponseDTO;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.entity.TopicEntity;
import com.goorm.team9.icontact.domain.client.enums.*;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientConverter clientConverter;
    private final ImageFileStorageService imageFileStorageService;

    @Transactional
    public ClientResponseDTO createMyPage(
            String nickName, Long age, String email,
            Industry industry, Role role, Career career, Status status,
            String introduction, String link, MultipartFile profileImage,
            Interest topic1, Interest topic2, Interest topic3, Language language, Framework framework
    ) {
        if (clientRepository.existsByEmail(email)) {
            throw new CustomException(ClientErrorCode.EXISTED_EMAIL);
        }

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setNickName(nickName);
        clientEntity.setAge(age);
        clientEntity.setEmail(email);
        clientEntity.setIndustry(industry);
        clientEntity.setRole(role);
        clientEntity.setCareer(career);
        clientEntity.setStatus(status);
        clientEntity.setIntroduction(introduction);
        clientEntity.setLink(link);

        if (profileImage != null && !profileImage.isEmpty()) {
            String imagePath = imageFileStorageService.storeFile(profileImage);
            clientEntity.setProfileImage(imagePath);
        } else {
            clientEntity.setProfileImage("/profile-images/default_profile_image.jpg");
        }

        TopicEntity topicEntity = new TopicEntity();
        topicEntity.setTopic1(topic1);
        topicEntity.setTopic2(topic2);
        topicEntity.setTopic3(topic3);
        topicEntity.setLanguage(language);
        topicEntity.setFramework(framework);

        topicEntity.setClient(clientEntity);
        clientEntity.setIt_topic(topicEntity);

        ClientEntity savedClient = clientRepository.save(clientEntity);

        return clientConverter.toResponseDTO(savedClient);
    }


    @Transactional(readOnly = true)
    public ClientResponseDTO getUserById(Long clientId) {
        ClientEntity clientEntity = clientRepository.findByIdAndIsDeletedFalse(clientId)
                .orElseThrow(() -> new CustomException(ClientErrorCode.CLIENT_NOT_FOUND));

        if (clientEntity.getStatus() == Status.PRIVATE) {
            throw new CustomException(ClientErrorCode.STATUS_IS_PRIVATE);
        }

        return clientConverter.toResponseDTO(clientEntity);
    }

    @Transactional
    public ClientResponseDTO updateUser(
            Long clientId, String nickName, Long age,
            Industry industry, Role role, Career career, Status status,
            String introduction, String link, MultipartFile profileImage,
            Interest topic1, Interest topic2, Interest topic3, Language language, Framework framework
    ) {
        ClientEntity existingClient = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException(ClientErrorCode.CLIENT_NOT_FOUND));

        if (nickName != null) existingClient.setNickName(nickName);
        if (age != null) existingClient.setAge(age);
        if (industry != null) existingClient.setIndustry(industry);
        if (role != null) existingClient.setRole(role);
        if (career != null) existingClient.setCareer(career);
        if (status != null) existingClient.setStatus(status);
        if (introduction != null) existingClient.setIntroduction(introduction);
        if (link != null) existingClient.setLink(link);

        TopicEntity topic = existingClient.getIt_topic();
        if (topic != null) {
            if (topic1 != null) topic.setTopic1(topic1);
            if (topic2 != null) topic.setTopic2(topic2);
            if (topic3 != null) topic.setTopic3(topic3);
            if (language != null) topic.setLanguage(language);
            if (framework != null) topic.setFramework(framework);
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            if (!existingClient.getProfileImage().equals("/profile-images/default_profile_image.jpg")) {
                imageFileStorageService.deleteFile(existingClient.getProfileImage());
            }
            String imagePath = imageFileStorageService.storeFile(profileImage);
            existingClient.setProfileImage(imagePath);
        }

        ClientEntity updatedClient = clientRepository.save(existingClient);
        return clientConverter.toResponseDTO(updatedClient);
    }

}
