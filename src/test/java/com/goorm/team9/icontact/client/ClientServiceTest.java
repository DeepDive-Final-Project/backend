package com.goorm.team9.icontact.client;

import com.goorm.team9.icontact.common.error.ClientErrorCode;
import com.goorm.team9.icontact.common.exception.CustomException;
import com.goorm.team9.icontact.domain.client.converter.ClientConverter;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageCreateRequest;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageUpdateRequest;
import com.goorm.team9.icontact.domain.client.dto.response.ClientProfileImageDTO;
import com.goorm.team9.icontact.domain.client.dto.response.ClientResponseDTO;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.entity.TopicEntity;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.enums.Status;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.client.service.ClientService;
import com.goorm.team9.icontact.domain.client.service.S3ImageStorageService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock private ClientRepository clientRepository;
    @Mock private ClientConverter clientConverter;
    @Mock private S3ImageStorageService imageStorageService;

    private ClientEntity mockClient;
    private TopicEntity mockTopic;
    private MultipartFile mockFile;

    @BeforeEach
    void setup() {
        mockTopic = TopicEntity.builder().build();
        mockClient = ClientEntity.builder()
                .id(1L)
                .nickName("Noah")
                .email("noah@example.com")
                .provider("kakao")
                .role(Role.DEV)
                .it_topic(mockTopic)
                .profileImage("profile.jpg")
                .links(new ArrayList<>())
                .build();

        mockFile = new MockMultipartFile("profileImage", "test.jpg", "image/jpeg", "fake-image".getBytes());
    }

    @Test
    @Order(1)
    @DisplayName("마이페이지 성공 테스트")
    void 마이페이지_생성_성공_테스트() {
        // given
        MyPageCreateRequest request = MyPageCreateRequest.builder()
                .email("noah@example.com")
                .nickName("Noah")
                .role(Role.DEV)
                .provider("kakao")
                .build();

        given(clientRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(imageStorageService.storeFile(mockFile)).willReturn("profile.jpg");
        given(clientRepository.save(any(ClientEntity.class))).willReturn(mockClient);
        given(clientConverter.toResponseDTO(any())).willReturn(new ClientResponseDTO());

        // when
        ClientResponseDTO result = clientService.createMyPage(request, mockFile);

        // then
        assertThat(result).isNotNull();
        verify(clientRepository).save(any());
    }

    @Test
    @Order(2)
    @DisplayName("공개 상태 사용자 정보 조회 테스트")
    void 사용자ID로_공개상태_사용자_조회_테스트() {
        // given
        given(clientRepository.findByIdAndIsDeletedFalse(1L)).willReturn(Optional.of(mockClient));
        given(clientConverter.toResponseDTO(mockClient)).willReturn(new ClientResponseDTO());

        // when
        ClientResponseDTO result = clientService.getUserById(1L);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("사용자 정보 업데이트 테스트")
    void 사용자_정보_업데이트_테스트() {
        // given
        MyPageUpdateRequest request = MyPageUpdateRequest.builder()
                .nickName("Updated")
                .build();

        given(clientRepository.findById(1L)).willReturn(Optional.of(mockClient));
        given(clientRepository.save(mockClient)).willReturn(mockClient);
        given(clientConverter.toResponseDTO(mockClient)).willReturn(new ClientResponseDTO());

        // when
        ClientResponseDTO result = clientService.updateUser(1L, request, mockFile);

        // then
        assertThat(result).isNotNull();
        verify(clientRepository).save(mockClient);
    }

    @Test
    @Order(4)
    @DisplayName("전체 사용자 조회 테스트")
    void 전체_사용자_조회_테스트() {
        given(clientRepository.findAllByIsDeletedFalse()).willReturn(List.of(mockClient));
        given(clientConverter.toResponseDTO(mockClient)).willReturn(new ClientResponseDTO());

        List<ClientResponseDTO> result = clientService.getAllClients();

        assertThat(result).isNotEmpty();
        verify(clientRepository).findAllByIsDeletedFalse();
    }

    @Test
    @Order(5)
    @DisplayName("프로필 미등록 생성 계정의 이미지 반환 테스트")
    void 마이페이지_생성_후_가본이미지_반환_테스트() {
        given(clientRepository.findByIdAndIsDeletedFalse(anyLong())).willReturn(Optional.empty());
        given(imageStorageService.getDefaultImage()).willReturn("default.jpg");

        List<ClientProfileImageDTO> result = clientService.getProfileImages(List.of(99L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProfileImageUrl()).isEqualTo("default.jpg");
    }
}