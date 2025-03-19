package com.goorm.team9.icontact.domain.location.service;

import com.goorm.team9.icontact.domain.client.enums.Interest;
import com.goorm.team9.icontact.domain.location.dto.LocationResponse;
import com.goorm.team9.icontact.common.exception.GlobalExceptionErrorCode;
import com.goorm.team9.icontact.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.geo.search-radius:10}")
    private double searchRadius;

    public boolean saveUserInformation(Long id, double latitude, double longitude, String interest) {
        validateId(id);
        validateLocationData(latitude, longitude);

        String userKey = "client:" + id;
        String globalKey = "location_data";

        redisTemplate.delete(userKey);
        redisTemplate.opsForZSet().remove(globalKey, id.toString());

        redisTemplate.opsForGeo().add(userKey, new Point(longitude, latitude), id.toString());
        redisTemplate.opsForGeo().add(globalKey, new Point(longitude, latitude), id.toString());

        updateUserInterest(id);

        Point savedPoint = redisTemplate.opsForGeo().position(globalKey, id.toString()).get(0);
        log.info("[REDIS 저장] client_id: {}, (위도: {}, 경도: {})", id, savedPoint.getY(), savedPoint.getX());

        return true;
    }

    private void validateId(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM client WHERE id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, new Object[]{id}, Boolean.class);
        if (exists == null || !exists) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_USER_ID);
        }
    }

    private void validateLocationData(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_LOCATION_DATA);
        }
    }

    public void updateUserInterest(Long id) {
        log.debug("updateUserInterest 검증 호출 - client_id: {}", id);

        String sql = "SELECT topic1, topic2, topic3 FROM it_topic WHERE client_id = ?";
        Map<String, Object> interestData;

        try {
            interestData = jdbcTemplate.queryForMap(sql, id);
        } catch (Exception e) {
            throw new CustomException(GlobalExceptionErrorCode.MISSING_INTEREST);
        }

        if (interestData.isEmpty() || interestData.values().stream().allMatch(value -> value == null || value.toString().isEmpty())) {
            throw new CustomException(GlobalExceptionErrorCode.MISSING_INTEREST);
        }

        String topic1 = interestData.get("topic1").toString();
        String topic2 = interestData.get("topic2").toString();
        String topic3 = interestData.get("topic3").toString();

        String redisKey = "interest:" + id;
        redisTemplate.opsForHash().putAll(redisKey, Map.of("topic1", topic1, "topic2", topic2, "topic3", topic3));

        log.info("[REDIS 관심분야 검증 완료] client_id: {}, topic1: {}, topic2: {}, topic3: {}", id, topic1, topic2, topic3);
    }

    public List<LocationResponse> getNearbyUsers(Long id) {
        List<Point> existingPoints = redisTemplate.opsForGeo().position("location_data", id.toString());
        if (existingPoints == null || existingPoints.isEmpty()) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_USER_ID);
        }
        Point userPoint = existingPoints.get(0);
        double latitude = userPoint.getY();
        double longitude = userPoint.getX();

        Map<Object, Object> cachedInterest = redisTemplate.opsForHash().entries("interest:" + id);
        if (cachedInterest.isEmpty()) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_INTEREST);
        }
        String interest = String.format("%s,%s,%s",
                cachedInterest.getOrDefault("topic1", ""),
                cachedInterest.getOrDefault("topic2", ""),
                cachedInterest.getOrDefault("topic3", "")
        );

        return findNearbyUsers(latitude, longitude, interest);
    }

    public List<LocationResponse> findNearbyUsers(double latitude, double longitude, String interest) {
        String key = "location_data";

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(
                        key,
                        new Circle(new Point(longitude, latitude), new Distance(searchRadius, RedisGeoCommands.DistanceUnit.METERS)),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().includeDistance()
                );

        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        List<LocationResponse> candidateList = new ArrayList<>();
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = results.getContent();
        int size = content.size();
        int i = 0;

        while (i < size) {
            GeoResult<RedisGeoCommands.GeoLocation<String>> result = content.get(i);
            Long targetId = Long.valueOf(result.getContent().getName());
            Map<String, String> targetInterest = getUserInterest(targetId);

            if (targetInterest.isEmpty()) {
                throw new CustomException(GlobalExceptionErrorCode.MISSING_INTEREST);
            }

            int matchScore = calculateInterestMatch(interest, targetInterest);
            Double distanceValue = result.getDistance() != null ? result.getDistance().getValue() : 0.0;

            LocationResponse response = new LocationResponse(
                    targetId,
                    result.getContent().getPoint().getY(),
                    result.getContent().getPoint().getX(),
                    distanceValue,
                    targetInterest.toString()
            );

            candidateList.add(response);
            i++;
        }

        candidateList.sort(new Comparator<LocationResponse>() {
            @Override
            public int compare(LocationResponse o1, LocationResponse o2) {
                int interestCompare = Integer.compare(
                        calculateInterestMatch(interest, getUserInterest(o2.getId())),
                        calculateInterestMatch(interest, getUserInterest(o1.getId()))
                );
                if (interestCompare != 0) {
                    return interestCompare;
                }
                return Double.compare(o1.getDistanceValue(), o2.getDistanceValue());
            }
        });

        List<LocationResponse> finalList = new ArrayList<>();
        int limit = Math.min(candidateList.size(), 10);
        int j = 0;
        while (j < limit) {
            finalList.add(candidateList.get(j));
            j++;
        }

        return finalList;
    }

    public List<LocationResponse> refreshNearbyUsers(Long id, double latitude, double longitude, String interest) {
        String userKey = "client:" + id;
        String globalKey = "location_data";

        List<Point> existingPoints = redisTemplate.opsForGeo().position(globalKey, id.toString());

        if (existingPoints != null && !existingPoints.isEmpty()) {
            Point existingPoint = existingPoints.get(0);
            double existingLatitude = existingPoint.getY();
            double existingLongitude = existingPoint.getX();

            if (latitude == existingLatitude && longitude == existingLongitude) {
                log.info("[REFRESH] client_id: {}, 위치 변경 없음 (위도: {}, 경도: {})", id, latitude, longitude);
                return getNearbyUsers(id);
            }
        }

        redisTemplate.delete(userKey);
        redisTemplate.opsForZSet().remove(globalKey, id.toString());

        redisTemplate.opsForGeo().add(userKey, new Point(longitude, latitude), id.toString());
        redisTemplate.opsForGeo().add(globalKey, new Point(longitude, latitude), id.toString());

        log.info("[REFRESH] client_id: {}, 위치 업데이트 완료 (위도: {}, 경도: {})", id, latitude, longitude);

        return getNearbyUsers(id);
    }

    private Map<String, String> getUserInterest(Long id) {
        String redisKey = "interest:" + id;

        Map<Object, Object> cachedInterest = redisTemplate.opsForHash().entries(redisKey);
        if (!cachedInterest.isEmpty()) {
            Map<String, String> interestMap = new HashMap<>();
            cachedInterest.forEach((key, value) -> interestMap.put(key.toString(), convertToDescription(value.toString())));
            return interestMap;
        }

        String sql = "SELECT topic1, topic2, topic3 FROM it_topic WHERE client_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
                Map<String, String> map = new HashMap<>();
                map.put("topic1", convertToDescription(rs.getString("topic1")));
                map.put("topic2", convertToDescription(rs.getString("topic2")));
                map.put("topic3", convertToDescription(rs.getString("topic3")));
                return map;
            });
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String convertToDescription(String enumName) {
        if (enumName == null || enumName.isEmpty()) {
            return GlobalExceptionErrorCode.INVALID_INTEREST.getDescription();
        }
        try {
            return Interest.valueOf(enumName).getDescription();
        } catch (IllegalArgumentException e) {
            return GlobalExceptionErrorCode.UNKNOWN_INTEREST.getFormattedMessage(enumName);
        }
    }

    private int calculateInterestMatch(String userInterest, Map<String, String> targetInterest) {
        int matchScore = 0;

        Set<String> userTopics = new LinkedHashSet<>(Arrays.asList(userInterest.split(",")));

        List<String> interestList = new ArrayList<>(targetInterest.values());
        int listSize = interestList.size();
        int index = 0;

        while (index < listSize) {
            String topic = interestList.get(index);
            if (topic != null && userTopics.contains(topic)) {
                matchScore++;
            }
            index++;
        }

        return matchScore;
    }
}