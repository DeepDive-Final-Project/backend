package com.goorm.team9.icontact.domain.client.service;

import com.goorm.team9.icontact.common.error.ClientErrorCode;
import com.goorm.team9.icontact.common.exception.CustomException;
import com.goorm.team9.icontact.domain.client.converter.ClientConverter;
import com.goorm.team9.icontact.domain.client.dto.request.ClientLinkRequestDto;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageCreateRequestDto;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageUpdateRequestDto;
import com.goorm.team9.icontact.domain.client.dto.response.ClientProfileImageDto;
import com.goorm.team9.icontact.domain.client.dto.response.ClientResponseDto;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.entity.ClientLinkEntity;
import com.goorm.team9.icontact.domain.client.entity.TopicEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientConverter clientConverter;
    private final S3ImageStorageService imageFileStorageService;

    @Transactional
    public ClientResponseDto createMyPage(MyPageCreateRequestDto request, MultipartFile profileImage) {
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ClientErrorCode.EXISTED_EMAIL);
        }

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setNickName(request.getNickName());
        clientEntity.setEmail(request.getEmail());
        clientEntity.setRole(request.getRole());
        clientEntity.setCareer(request.getCareer());
        clientEntity.setIntroduction(request.getIntroduction());

        List<ClientLinkEntity> linkEntities = new ArrayList<>();
        if (request.getLinks() != null) {
            for (ClientLinkRequestDto linkDto : request.getLinks()) {
                ClientLinkEntity linkEntity = ClientLinkEntity.builder()
                        .title(linkDto.getTitle())
                        .link(linkDto.getLink())
                        .client(clientEntity)
                        .build();
                linkEntities.add(linkEntity);
            }
        }
        clientEntity.setLinks(linkEntities);

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
        clientEntity.setProvider(request.getProvider());

        topicEntity.setClient(clientEntity);
        clientEntity.setIt_topic(topicEntity);

        ClientEntity savedClient = clientRepository.save(clientEntity);

        return clientConverter.toResponseDTO(savedClient);
    }

    @Transactional(readOnly = true)
    public ClientResponseDto getUserById(Long clientId) {
        ClientEntity clientEntity = clientRepository.findByIdAndIsDeletedFalse(clientId)
                .orElseThrow(() -> new CustomException(ClientErrorCode.CLIENT_NOT_FOUND));

        return clientConverter.toResponseDTO(clientEntity);
    }

    @Transactional
    public ClientResponseDto updateUser(Long clientId, MyPageUpdateRequestDto request, MultipartFile profileImage) {
        ClientEntity existingClient = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException(ClientErrorCode.CLIENT_NOT_FOUND));

        if (request.getNickName() != null) existingClient.setNickName(request.getNickName());
        if (request.getRole() != null) existingClient.setRole(request.getRole());
        if (request.getCareer() != null) existingClient.setCareer(request.getCareer());
        if (request.getIntroduction() != null) existingClient.setIntroduction(request.getIntroduction());

        existingClient.getLinks().clear();

        if (request.getLinks() != null) {
            List<ClientLinkEntity> updatedLinks = request.getLinks().stream()
                    .map(linkDto -> ClientLinkEntity.builder()
                            .title(linkDto.getTitle())
                            .link(linkDto.getLink())
                            .client(existingClient)
                            .build())
                    .toList();

            existingClient.getLinks().addAll(updatedLinks);
        }

        TopicEntity topic = existingClient.getIt_topic();

        if (topic == null) {
            TopicEntity newTopic = TopicEntity.builder()
                    .topic1(request.getTopic1())
                    .topic2(request.getTopic2())
                    .topic3(request.getTopic3())
                    .client(existingClient)
                    .build();
            existingClient.setIt_topic(newTopic);
        } else {
            if (request.getTopic1() != null) topic.setTopic1(request.getTopic1());
            if (request.getTopic2() != null) topic.setTopic2(request.getTopic2());
            if (request.getTopic3() != null) topic.setTopic3(request.getTopic3());
        }

        String currentImage = existingClient.getProfileImage();

        if (profileImage != null && !profileImage.isEmpty()) {
            if (!imageFileStorageService.isDefaultImage(currentImage)) {
                imageFileStorageService.deleteFile(currentImage);
            }
            String imagePath = imageFileStorageService.storeFile(profileImage);
            existingClient.setProfileImage(imagePath);

        } else if ((profileImage == null || profileImage.isEmpty()) && currentImage == null) {
            existingClient.setProfileImage(imageFileStorageService.getDefaultImage());

        } else if ((profileImage == null || profileImage.isEmpty()) && !imageFileStorageService.isDefaultImage(currentImage)) {
            imageFileStorageService.deleteFile(currentImage);
            existingClient.setProfileImage(imageFileStorageService.getDefaultImage());
        }

        ClientEntity updatedClient = clientRepository.save(existingClient);
        return clientConverter.toResponseDTO(updatedClient);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getAllClients() {
        List<ClientEntity> clients = clientRepository.findAllByIsDeletedFalse();
        return clients.stream()
                .map(clientConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientProfileImageDto> getProfileImages(List<Long> clientIds) {
        return clientIds.stream()
                .map(id -> clientRepository.findByIdAndIsDeletedFalse(id)
                        .map(client -> new ClientProfileImageDto(client.getId(),
                                client.getProfileImage() != null ? client.getProfileImage() : imageFileStorageService.getDefaultImage()))
                        .orElse(new ClientProfileImageDto(id, imageFileStorageService.getDefaultImage())))
                .toList();
    }

}
