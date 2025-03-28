package com.goorm.team9.icontact.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.team9.icontact.domain.location.controller.LocationController;
import com.goorm.team9.icontact.domain.location.dto.*;
import com.goorm.team9.icontact.domain.location.service.LocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LocationController.class)
@Import(LocationControllerTest.MockConfig.class)
class LocationControllerTest {

    private static final LocationService locationService = Mockito.mock(LocationService.class);

    @TestConfiguration
    static class MockConfig {
        @Bean
        public LocationService locationService() {
            return locationService;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("위치 저장 API 테스트")
    void saveLocationTest() throws Exception {
        LocationRequest request = new LocationRequest(1L, 37.402056, 127.108212);

        mockMvc.perform(post("/api/location/save")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("위치 데이터가 저장되었습니다."))
                .andExpect(jsonPath("$.id").value(1));

        verify(locationService).saveUserInformation(1L, 37.402056, 127.108212);
    }

    @Test
    @WithMockUser
    @DisplayName("위치 삭제 API 테스트")
    void deleteLocationTest() throws Exception {
        DeleteRequest request = new DeleteRequest(1L);

        mockMvc.perform(delete("/api/location/delete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("위치 데이터가 삭제되었습니다."))
                .andExpect(jsonPath("$.id").value(1));

        verify(locationService).deleteUserLocation(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("근처 참가자 조회 API 테스트")
    void getNearbyUsersTest() throws Exception {
        NearbyRequest request = new NearbyRequest(1L, "개발자", "주니어");
        List<LocationResponse> dummyList = Collections.singletonList(
                new LocationResponse(2L, 37.402100, 127.108300, 5.0,
                        "백엔드,프론트엔드,AI", "개발자", "주니어",
                        "홍일동", "AI를 좋아하는 백엔드 개발자입니다.")
        );

        when(locationService.getNearbyUsers(anyLong(), any(), any())).thenReturn(dummyList);

        mockMvc.perform(post("/api/location/nearby")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("근처 참가자 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data[0].id").value(2));
    }

    @Test
    @WithMockUser
    @DisplayName("근처 참가자 재조회 API 테스트")
    void refreshNearbyUsersTest() throws Exception {
        RefreshRequest request = new RefreshRequest(1L, 37.402056, 127.108212, "개발자", "주니어");
        List<LocationResponse> dummyList = Collections.singletonList(
                new LocationResponse(3L, 37.402200, 127.108400, 4.5,
                        "백엔드,AI,보안", "개발자", "주니어",
                        "홍이동", "보안과 AI에 관심 많은 주니어입니다.")
        );

        when(locationService.refreshNearbyUsers(anyLong(), anyDouble(), anyDouble(), any(), any()))
                .thenReturn(dummyList);

        mockMvc.perform(post("/api/location/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("주변 참가자 재조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data[0].id").value(3));
    }

    @Test
    @WithMockUser
    @DisplayName("근처 참가자 조회 API - 반경 10m 내 17명, 외부 11명 존재")
    void getNearbyUsersTest_withManyParticipants() throws Exception {
        NearbyRequest request = new NearbyRequest(1L, "개발자", "주니어");

        List<LocationResponse> nearbyUsers = IntStream.rangeClosed(1, 17)
                .mapToObj(i -> new LocationResponse(
                        (long) i,
                        37.402056 + i * 0.000001,
                        127.108212 + i * 0.000001,
                        5.0,
                        "백엔드,AI",
                        "개발자",
                        "주니어",
                        "참가자" + i,
                        "소개 메시지 " + i))
                .toList();

        when(locationService.getNearbyUsers(anyLong(), any(), any())).thenReturn(nearbyUsers);

        mockMvc.perform(post("/api/location/nearby")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("근처 참가자 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.length()").value(17))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[16].id").value(17));
    }

    @Test
    @WithMockUser
    @DisplayName("근처 참가자 조회 API - 반경 10m 내 + 관심분야 + role + career 일치하는 참가자만 반환")
    void getNearbyUsers_withAllConditionsMatch() throws Exception {
        NearbyRequest request = new NearbyRequest(1L, "개발자", "주니어");

        List<LocationResponse> matchedUsers = List.of(
                new LocationResponse(10L, 37.402100, 127.108210, 4.0,
                        "백엔드,AI", "개발자", "주니어",
                        "홍삼동", "AI에 진심인 백엔드 주니어입니다."),
                new LocationResponse(11L, 37.402105, 127.108215, 3.5,
                        "보안,AI", "개발자", "주니어",
                        "홍사동", "보안전문가를 꿈꾸는 개발자입니다."),
                new LocationResponse(12L, 37.402110, 127.108220, 3.0,
                        "AI", "개발자", "주니어",
                        "홍오동", "AI 전공한 주니어입니다.")
        );

        when(locationService.getNearbyUsers(eq(1L), eq("개발자"), eq("주니어")))
                .thenReturn(matchedUsers);

        mockMvc.perform(post("/api/location/nearby")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("근처 참가자 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].role").value("개발자"))
                .andExpect(jsonPath("$.data[0].career").value("주니어"))
                .andExpect(jsonPath("$.data[0].interest").value(org.hamcrest.Matchers.containsString("AI")));
    }
}
