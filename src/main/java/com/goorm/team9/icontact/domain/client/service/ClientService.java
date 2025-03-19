package com.goorm.team9.icontact.domain.client.service;

import com.goorm.team9.icontact.common.error.ClientErrorCode;
import com.goorm.team9.icontact.common.exception.CustomException;
import com.goorm.team9.icontact.domain.client.converter.ClientConverter;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageCreateRequest;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageUpdateRequest;
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
    private final S3ImageStorageService imageFileStorageService;

    @Transactional
    public ClientResponseDTO createMyPage(MyPageCreateRequest request, MultipartFile profileImage) {
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ClientErrorCode.EXISTED_EMAIL);
        }

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setNickName(request.getNickName());
        clientEntity.setEmail(request.getEmail());
        clientEntity.setRole(request.getRole());
        clientEntity.setCareer(request.getCareer());
        clientEntity.setStatus(request.getStatus());
        clientEntity.setIntroduction(request.getIntroduction());
        clientEntity.setLink(request.getLink());

        if (profileImage != null && !profileImage.isEmpty()) {
            String imagePath = imageFileStorageService.storeFile(profileImage);
            clientEntity.setProfileImage(imagePath);
        } else {
            clientEntity.setProfileImage(imageFileStorageService.getDefaultImage());
        }

        TopicEntity topicEntity = new TopicEntity();
        topicEntity.setTopic1(request.getTopic1());
        topicEntity.setTopic2(request.getTopic2());
        topicEntity.setTopic3(request.getTopic3());
        topicEntity.setLanguage(request.getLanguage());
        topicEntity.setFramework(request.getFramework());

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
    public ClientResponseDTO updateUser(Long clientId, MyPageUpdateRequest request, MultipartFile profileImage) {
        ClientEntity existingClient = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException(ClientErrorCode.CLIENT_NOT_FOUND));

        if (request.getNickName() != null) existingClient.setNickName(request.getNickName());
        if (request.getRole() != null) existingClient.setRole(request.getRole());
        if (request.getCareer() != null) existingClient.setCareer(request.getCareer());
        if (request.getStatus() != null) existingClient.setStatus(request.getStatus());
        if (request.getIntroduction() != null) existingClient.setIntroduction(request.getIntroduction());
        if (request.getLink() != null) existingClient.setLink(request.getLink());

        TopicEntity topic = existingClient.getIt_topic();
        if (topic != null) {
            if (request.getTopic1() != null) topic.setTopic1(request.getTopic1());
            if (request.getTopic2() != null) topic.setTopic2(request.getTopic2());
            if (request.getTopic3() != null) topic.setTopic3(request.getTopic3());
            if (request.getLanguage() != null) topic.setLanguage(request.getLanguage());
            if (request.getFramework() != null) topic.setFramework(request.getFramework());
        }

        // 기존 이미지 삭제 후 새 이미지 저장
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
            existingClient.setProfileImage(imageFileStorageService.getDefaultImage());
        }

        ClientEntity updatedClient = clientRepository.save(existingClient);
        return clientConverter.toResponseDTO(updatedClient);
    }

}
