package com.goorm.team9.icontact.domain.location.service;

import com.goorm.team9.icontact.common.exception.CustomException;
import com.goorm.team9.icontact.common.exception.GlobalExceptionErrorCode;
import com.goorm.team9.icontact.domain.client.enums.Career;
import com.goorm.team9.icontact.domain.client.enums.Interest;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.location.dto.response.LocationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.geo.search-radius:1000}")
    private double searchRadius;

    public boolean saveUserInformation(Long id, double latitude, double longitude) {
        validateId(id);
        validateLocationData(latitude, longitude);

        String userKey = "client:" + id;
        String globalKey = "location_data";

        redisTemplate.delete(userKey);
        redisTemplate.opsForZSet().remove(globalKey, id.toString());

        redisTemplate.opsForGeo().add(userKey, new Point(longitude, latitude), id.toString());
        redisTemplate.opsForGeo().add(globalKey, new Point(longitude, latitude), id.toString());

        // redisTemplate.expire(userKey, Duration.ofSeconds(600));
        // redisTemplate.expire(globalKey, Duration.ofSeconds(600));

        updateUserInterest(id);

        List<Point> savedPoints = redisTemplate.opsForGeo().position(globalKey, id.toString());
        if (savedPoints == null || savedPoints.isEmpty()) {
            throw new CustomException(GlobalExceptionErrorCode.REDIS_SAVE_FAILURE);
        }
        Point savedPoint = savedPoints.get(0);
        log.info("[참가자 위치 저장] client_id: {}, 위도: {}, 경도: {}", id, savedPoint.getY(), savedPoint.getX());

        return true;
    }

    public void deleteUserLocation(Long id) {
        String userKey = "client:" + id;
        String globalKey = "location_data";

        redisTemplate.delete(userKey);
        redisTemplate.opsForZSet().remove(globalKey, id.toString());

        log.info("[참가자 위치 삭제] client_id: {}", id);
    }

    private void validateId(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM client WHERE id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, new Object[]{id}, Boolean.class);
        if (exists == null || !exists) {
            throw new CustomException(GlobalExceptionErrorCode.CLIENT_NOT_FOUND);
        }
    }

    private void validateLocationData(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_LOCATION_DATA);
        }
    }

    public void updateUserInterest(Long id) {
        String sql = "SELECT topic1, topic2, topic3 FROM it_topic WHERE client_id = ?";
        Map<String, Object> interestData = jdbcTemplate.queryForMap(sql, id);

        if (interestData.isEmpty()) {
            throw new CustomException(GlobalExceptionErrorCode.MISSING_INTEREST);
        }

        String redisKey = "interest:" + id;
        redisTemplate.opsForHash().putAll(redisKey, interestData);

        log.info("[참가자 관심분야 저장] client_id: {}, topic1: {}, topic2: {}, topic3: {}",
                id, interestData.get("topic1"), interestData.get("topic2"), interestData.get("topic3"));
    }

    public List<LocationResponseDto> getNearbyUsers(Long id, String roleDesc, String careerDesc) {
        syncInterestFromMySQLIfChanged(id);

        Point userPoint = getUserPoint(id);
        String interest = getFormattedInterest(id);

        return findNearbyUsers(id, userPoint.getY(), userPoint.getX(), interest, roleDesc, careerDesc);

    }

    public List<LocationResponseDto> refreshNearbyUsers(Long id, double latitude, double longitude, String roleDesc, String careerDesc) {
        String userKey = "client:" + id;
        String globalKey = "location_data";

        Point existingPoint = getUserPoint(id);

        if (Math.abs(latitude - existingPoint.getY()) < 0.000001 && Math.abs(longitude - existingPoint.getX()) < 0.000001) {
            return getNearbyUsers(id, roleDesc, careerDesc);
        }

        redisTemplate.delete(userKey);
        redisTemplate.opsForZSet().remove(globalKey, id.toString());

        redisTemplate.opsForGeo().add(userKey, new Point(longitude, latitude), id.toString());
        redisTemplate.opsForGeo().add(globalKey, new Point(longitude, latitude), id.toString());

//        redisTemplate.expire(userKey, Duration.ofSeconds(600));
//        redisTemplate.expire(globalKey, Duration.ofSeconds(600));

        log.info("[참가자 위치 새로고침] client_id: {}, 위치 갱신됨 (위도: {}, 경도: {})", id, latitude, longitude);

        return getNearbyUsers(id, roleDesc, careerDesc);
    }

    public List<LocationResponseDto> findNearbyUsers(Long currentUserId, double latitude, double longitude, String interest, String roleDesc, String careerDesc) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(
                "location_data",
                new Circle(new Point(longitude, latitude), new Distance(searchRadius, RedisGeoCommands.DistanceUnit.METERS)),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().includeDistance()
        );

        if (results == null || results.getContent().isEmpty()) return Collections.emptyList();

        List<LocationResponseDto> allCandidates = new ArrayList<>();

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> resultList = results.getContent();
        for (int i = 0; i < resultList.size(); i++) {
            GeoResult<RedisGeoCommands.GeoLocation<String>> result = resultList.get(i);
            RedisGeoCommands.GeoLocation<String> geoLocation = result.getContent();

            Long targetId = Long.valueOf(geoLocation.getName());
            if (targetId.equals(currentUserId)) continue;

            Map<String, String> targetInterest = getUserInterest(targetId);
            int matchScore = calculateInterestMatch(interest, targetInterest);

            Map<String, Object> rcInfo = jdbcTemplate.queryForMap(
                    "SELECT role, career, nick_name, introduction FROM client WHERE id = ?", targetId);
            String roleEnumName = rcInfo.getOrDefault("role", "").toString();
            String careerEnumName = rcInfo.getOrDefault("career", "").toString();

            String targetRole;
            String targetCareer;

            try {
                targetRole = Role.valueOf(roleEnumName).getDescription();
            } catch (IllegalArgumentException e) {
                targetRole = roleEnumName;
            }

            try {
                targetCareer = Career.valueOf(careerEnumName).getDescription();
            } catch (IllegalArgumentException e) {
                targetCareer = careerEnumName;
            }

            String nickname = rcInfo.getOrDefault("nick_name", "").toString();
            String introduction = rcInfo.getOrDefault("introduction", "").toString();

            double distance = result.getDistance().getValue();

            String formattedInterest = targetInterest.entrySet().stream()
                    .map(e -> e.getKey() + " : " + e.getValue())
                    .collect(Collectors.joining(", "));

            LocationResponseDto response = new LocationResponseDto(
                    targetId,
                    geoLocation.getPoint().getY(),
                    geoLocation.getPoint().getX(),
                    distance,
                    formattedInterest,
                    targetRole,
                    targetCareer,
                    nickname,
                    introduction
            );

            response.setDistanceValue(distance);
            response.setMatchScore(matchScore);
            allCandidates.add(response);
        }

        Stream<LocationResponseDto> filteredStream = allCandidates.stream();
        if (roleDesc != null || careerDesc != null) {
            filteredStream = filteredStream.filter(r -> isRoleCareerMatch(r.getId(), roleDesc, careerDesc));
        }

        List<LocationResponseDto> sorted = filteredStream
                .sorted(Comparator
                        .comparingDouble(LocationResponseDto::getDistanceValue)
                        .thenComparing(Comparator.comparingInt(LocationResponseDto::getMatchScore).reversed())
                )
                .limit(10)
                .collect(Collectors.toList());

        return sorted;
    }

    private Point getUserPoint(Long id) {
        List<Point> points = redisTemplate.opsForGeo().position("location_data", id.toString());
        if (points == null || points.isEmpty()) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_USER_ID);
        }
        return points.get(0);
    }

    private String getFormattedInterest(Long id) {
        Map<Object, Object> cached = redisTemplate.opsForHash().entries("interest:" + id);
        return String.format("%s,%s,%s",
                cached.getOrDefault("topic1", ""),
                cached.getOrDefault("topic2", ""),
                cached.getOrDefault("topic3", "")
        );
    }

    private void syncInterestFromMySQLIfChanged(Long id) {
        Map<Object, Object> cached = redisTemplate.opsForHash().entries("interest:" + id);
        Map<String, Object> db = jdbcTemplate.queryForMap("SELECT topic1, topic2, topic3 FROM it_topic WHERE client_id = ?", id);

        boolean isDifferent = db.entrySet().stream()
                .anyMatch(e -> !Objects.equals(e.getValue(), cached.getOrDefault(e.getKey(), "")));

        if (isDifferent) {
            redisTemplate.opsForHash().putAll("interest:" + id, db);
            log.info("[참가자 관심분야 동기화] client_id: {}, Redis가 최신 MySQL 기준으로 갱신됨", id);
        }
    }

    private boolean isRoleCareerMatch(Long id, String roleDesc, String careerDesc) {
        if (roleDesc == null && careerDesc == null) return true;

        Map<String, Object> rc = jdbcTemplate.queryForMap("SELECT role, career FROM client WHERE id = ?", id);
        String userRole = rc.getOrDefault("role", "").toString();
        String userCareer = rc.getOrDefault("career", "").toString();

        boolean roleMatch = true;
        boolean careerMatch = true;

        if (roleDesc != null) {
            try {
                Role roleEnum = Role.fromDescription(roleDesc.trim());
                roleMatch = roleEnum.name().equalsIgnoreCase(userRole);
            } catch (IllegalArgumentException e) {
                throw new CustomException(GlobalExceptionErrorCode.INVALID_ROLE);
            }
        }

        if (careerDesc != null) {
            try {
                Career careerEnum = Career.fromDescription(careerDesc.trim());
                careerMatch = careerEnum.name().equalsIgnoreCase(userCareer);
            } catch (IllegalArgumentException e) {
                throw new CustomException(GlobalExceptionErrorCode.INVALID_CAREER);
            }
        }

        return roleMatch && careerMatch;
    }

    private Map<String, String> getUserInterest(Long id) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries("interest:" + id);
        Map<String, String> interestMap = new HashMap<>();

        List<Map.Entry<Object, Object>> entryList = new ArrayList<>(raw.entrySet());
        for (int i = 0; i < entryList.size(); i++) {
            Map.Entry<Object, Object> entry = entryList.get(i);
            String key = entry.getKey().toString();
            String value = convertToDescription(entry.getValue().toString());
            interestMap.put(key, value);
        }

        return interestMap;
    }

    private String convertToDescription(String enumName) {
        if (enumName == null || enumName.isEmpty()) return GlobalExceptionErrorCode.INVALID_INTEREST.getDescription();
        try {
            return Interest.valueOf(enumName).getDescription();
        } catch (IllegalArgumentException e) {
            return GlobalExceptionErrorCode.UNKNOWN_INTEREST.getFormattedMessage(enumName);
        }
    }

    private int calculateInterestMatch(String userInterest, Map<String, String> targetInterest) {
        int score = 0;
        Set<String> userTopics = new HashSet<>(Arrays.asList(userInterest.split(",")));
        List<String> topicList = new ArrayList<>(targetInterest.values());
        for (int i = 0; i < topicList.size(); i++) {
            String topic = topicList.get(i);
            if (userTopics.contains(topic)) {
                score++;
            }
        }
        return score;
    }
}
