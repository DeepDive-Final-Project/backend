package com.goorm.team9.icontact.domain.client.service;

import com.goorm.team9.icontact.common.error.ClientErrorCode;
import com.goorm.team9.icontact.common.exception.CustomException;
import com.goorm.team9.icontact.domain.client.converter.ClientConverter;
import com.goorm.team9.icontact.domain.client.dto.response.ClientResponseDTO;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.entity.TopicEntity;
import com.goorm.team9.icontact.domain.client.enums.*;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
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
            String nickName, String email,
            Role role, Career career, Status status,
            String introduction, String link, MultipartFile profileImage,
            Interest topic1, Interest topic2, Interest topic3, Language language, Framework framework
    ) {
        if (clientRepository.existsByEmail(email)) {
            throw new CustomException(ClientErrorCode.EXISTED_EMAIL);
        }

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setNickName(nickName);
        clientEntity.setEmail(email);
        clientEntity.setRole(role);
        clientEntity.setCareer(career);
        clientEntity.setStatus(status);
        clientEntity.setIntroduction(introduction);
        clientEntity.setLink(link);
        clientEntity.setChatOpportunity(5);

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
            Long clientId, String nickName,
            Role role, Career career, Status status,
            String introduction, String link, MultipartFile profileImage,
            Interest topic1, Interest topic2, Interest topic3, Language language, Framework framework
    ) {
        ClientEntity existingClient = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException(ClientErrorCode.CLIENT_NOT_FOUND));

        if (nickName != null) existingClient.setNickName(nickName);
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

        String currentImage = existingClient.getProfileImage();

        if (profileImage != null && !profileImage.isEmpty()) {
            if (!imageFileStorageService.isDefaultImage(currentImage)) {
                imageFileStorageService.deleteFile(currentImage);
            }
            String imagePath = imageFileStorageService.storeFile(profileImage);
            existingClient.setProfileImage(imagePath);
        }

        if (profileImage == null && !imageFileStorageService.isDefaultImage(currentImage)) {
            imageFileStorageService.deleteFile(currentImage);
            existingClient.setProfileImage("/profile-images/default_profile_image.jpg");
        }

        ClientEntity updatedClient = clientRepository.save(existingClient);
        return clientConverter.toResponseDTO(updatedClient);
    }

    @Transactional
    public void reduceChatOpportunity(Long clientId) {
        ClientEntity clientEntity = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException(ClientErrorCode.CLIENT_NOT_FOUND));

        if (clientEntity.getChatOpportunity() <= 0) {
            throw new CustomException(ClientErrorCode.NO_CHAT_OPPORTUNITY);
        }

        clientEntity.setChatOpportunity(clientEntity.getChatOpportunity() - 1);
        clientRepository.save(clientEntity);
    }

    @Transactional
    public void increaseChatOpportunity(Long clientId) {
        ClientEntity clientEntity = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException(ClientErrorCode.CLIENT_NOT_FOUND));

        if (clientEntity.getChatOpportunity() >= 5) {
            throw new CustomException(ClientErrorCode.CHAT_OPPORTUNITY_FULL);
        }

        clientEntity.setChatOpportunity(clientEntity.getChatOpportunity() + 1);
        clientRepository.save(clientEntity);
    }


}
