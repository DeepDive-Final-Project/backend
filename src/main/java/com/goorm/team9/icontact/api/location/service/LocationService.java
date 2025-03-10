package com.goorm.team9.icontact.api.location.service;

import com.goorm.team9.icontact.api.location.dto.LocationRequest;
import com.goorm.team9.icontact.api.location.dto.LocationResponse;
import com.goorm.team9.icontact.common.exception.GlobalExceptionErrorCode;
import com.goorm.team9.icontact.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.geo.search-radius:10}")
    private double searchRadius;

    public void saveLocation(LocationRequest request) {
        try {
            validateLocationData(request);
            validateUserId(request.getUserId());

            String key = "offline_users";

            Boolean exists = redisTemplate.opsForGeo().position(key, request.getUserId()).size() > 0;

            if (Boolean.TRUE.equals(exists)) {
                System.out.println("📌 기존 데이터 삭제: " + request.getUserId());
                redisTemplate.opsForZSet().remove(key, request.getUserId());
            }

            System.out.println("📌 Redis 위치 데이터 저장 시도: " + request.getUserId());
            redisTemplate.opsForGeo().add(key, new Point(request.getLongitude(), request.getLatitude()), request.getUserId());

            saveUserInterest(request.getUserId(), request.getInterest());

            System.out.println("✅ 위치 데이터 저장 완료: " + request.getUserId());
        } catch (Exception e) {
            System.err.println("❌ 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new CustomException(GlobalExceptionErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    public List<LocationResponse> getNearbyUsers(double latitude, double longitude, String userId) {
        String key = "offline_users";
        double radius = 10;

        Map<String, String> userInterest = getUserInterest(userId);
        if (userInterest.isEmpty()) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_USER_ID);
        }

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(
                        key,
                        new Circle(new Point(longitude, latitude), new Distance(radius, RedisGeoCommands.DistanceUnit.METERS)),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().includeDistance()
                );

        List<LocationResponse> filteredUsers = new ArrayList<>();
        Map<String, Integer> interestMatchCount = new HashMap<>();

        if (results != null && !results.getContent().isEmpty()) {
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results.getContent()) {
                String targetUserId = result.getContent().getName();
                Map<String, String> targetInterest = getUserInterest(targetUserId);

                if (!targetInterest.isEmpty()) {
                    int matchScore = calculateInterestMatch(userInterest, targetInterest);
                    if (matchScore > 0) {
                        Double distanceValue = 0.0;

                        if (result.getDistance() != null) {
                            distanceValue = result.getDistance().getValue();
                        }

                        filteredUsers.add(new LocationResponse(
                                targetUserId,
                                result.getContent().getPoint().getY(),
                                result.getContent().getPoint().getX(),
                                distanceValue,
                                targetInterest.toString()
                        ));

                        interestMatchCount.put(targetUserId, matchScore);
                    }
                }
            }
        }

        filteredUsers.sort((u1, u2) -> interestMatchCount.get(u2.getUserId()) - interestMatchCount.get(u1.getUserId()));

        while (filteredUsers.size() > 10) {
            int currentMaxMatch = interestMatchCount.values().stream().max(Integer::compareTo).orElse(0);

            List<LocationResponse> highMatchUsers = new ArrayList<>();
            for (LocationResponse user : filteredUsers) {
                if (interestMatchCount.get(user.getUserId()) == currentMaxMatch) {
                    highMatchUsers.add(user);
                }
            }

            if (highMatchUsers.size() <= 10) {
                filteredUsers = highMatchUsers;
                break;
            }

            filteredUsers = highMatchUsers;
        }

        if (filteredUsers.size() > 10) {
            Collections.shuffle(filteredUsers);
            return filteredUsers.subList(0, 10);
        }

        return filteredUsers;
    }

    private void saveUserInterest(String userId, String interest) {
        try {
            String sql = "INSERT INTO it_topic (user_id, topic1, topic2, topic3) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE topic1 = VALUES(topic1), topic2 = VALUES(topic2), topic3 = VALUES(topic3)";
            System.out.println("Executing SQL: " + sql + " with values: " + userId + ", " + interest);
            jdbcTemplate.update(sql, userId, interest, "", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(GlobalExceptionErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, String> getUserInterest(String userId) {
        String sql = "SELECT topic1, topic2, topic3 FROM it_topic WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId}, (rs, rowNum) -> {
            Map<String, String> interestMap = new HashMap<>();
            interestMap.put("topic1", rs.getString("topic1"));
            interestMap.put("topic2", rs.getString("topic2"));
            interestMap.put("topic3", rs.getString("topic3"));
            return interestMap;
        });
    }

    private int calculateInterestMatch(Map<String, String> userInterest, Map<String, String> targetInterest) {
        int matchScore = 0;
        for (String topic : userInterest.values()) {
            if (targetInterest.containsValue(topic)) {
                matchScore++;
            }
        }
        return matchScore;
    }

    private void validateLocationData(LocationRequest request) {
        if (request.getLatitude() < -90 || request.getLatitude() > 90 ||
                request.getLongitude() < -180 || request.getLongitude() > 180) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_LOCATION_DATA);
        }

        if (request.getLatitude() == 0.0 && request.getLongitude() == 0.0) {
            throw new CustomException(GlobalExceptionErrorCode.GPS_ERROR);
        }
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_USER_ID);
        }
    }
}