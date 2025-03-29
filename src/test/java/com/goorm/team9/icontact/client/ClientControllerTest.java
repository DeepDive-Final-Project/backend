package com.goorm.team9.icontact.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.team9.icontact.domain.client.controller.ClientController;
import com.goorm.team9.icontact.domain.client.converter.ClientConverter;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageCreateRequest;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageUpdateRequest;
import com.goorm.team9.icontact.domain.client.dto.response.ClientProfileImageDTO;
import com.goorm.team9.icontact.domain.client.dto.response.ClientResponseDTO;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.enums.Status;
import com.goorm.team9.icontact.domain.client.service.ClientService;
import com.goorm.team9.icontact.domain.client.service.S3ImageStorageService;
import org.apiguardian.api.API;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "testUser")
@WebMvcTest(controllers = ClientController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
        })
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3ImageStorageService s3ImageStorageService;

    @MockitoBean
    private ClientConverter clientConverter;

    @MockitoBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("마이페이지 생성 API 성공 테스트")
    void 마이페이지_생성_테스트() throws Exception {
        MyPageCreateRequest request = MyPageCreateRequest.builder()
                .email("test@example.com")
                .nickName("테스터")
                .role(Role.DEV)
                .provider("kakao")
                .build();

        MockMultipartFile image = new MockMultipartFile("profileImage", "test.jpg", "image/jpeg", "img".getBytes());
        MockMultipartFile userData = new MockMultipartFile("userData", "", "text/plain",
                objectMapper.writeValueAsString(request).getBytes());

        when(clientService.createMyPage(any(), any())).thenReturn(new ClientResponseDTO());

        mockMvc.perform(multipart("/api/client/profile")
                        .file(image)
                        .file(userData)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("마이페이지 수정 API 성공 테스트")
    void 마이페이지_수정_테스트() throws Exception {
        MyPageUpdateRequest request = MyPageUpdateRequest.builder()
                .nickName("업데이트됨")
                .build();

        MockMultipartFile image = new MockMultipartFile("profileImage", "update.jpg", "image/jpeg", "img".getBytes());
        MockMultipartFile userData = new MockMultipartFile("userData", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        when(clientService.updateUser(eq(1L), any(), any())).thenReturn(new ClientResponseDTO());

        mockMvc.perform(multipart("/api/client/profile/update/1")
                        .file(image)
                        .file(userData)
                        .with(csrf())
                        .with(req -> {
                            req.setMethod("PATCH");
                            return req;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("특정 사용자 조회 API 성공 테스트")
    void 사용자_조회_테스트() throws Exception {
        when(clientService.getUserById(1L)).thenReturn(new ClientResponseDTO());

        mockMvc.perform(get("/api/client/profile/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("전체 사용자 조회 API 성공 테스트")
    void 전체_사용자_조회_테스트() throws Exception {
        when(clientService.getAllClients()).thenReturn(List.of(new ClientResponseDTO()));

        mockMvc.perform(get("/api/client/profile/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("프로필 이미지 목록 조회 API 성공 테스트")
    void 프로필_이미지_목록_조회_테스트() throws Exception {
        when(clientService.getProfileImages(any())).thenReturn(List.of(
                new ClientProfileImageDTO(1L, "https://test-image.com/profile.jpg")
        ));

        mockMvc.perform(get("/api/client/profile/profile-images")
                        .param("clientIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("clientId 10개 초과시 실패 테스트")
    void 프로필_이미지_목록_조회_실패_테스트() throws Exception {
        mockMvc.perform(get("/api/client/profile/profile-images")
                        .param("clientIds", "1,2,3,4,5,6,7,8,9,10,11"))
                .andExpect(status().isBadRequest());
    }
}
