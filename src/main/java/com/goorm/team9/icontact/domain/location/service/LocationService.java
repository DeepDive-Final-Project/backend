package com.goorm.team9.icontact.domain.location.service;

import com.goorm.team9.icontact.domain.location.dto.LocationResponse;
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

    public boolean saveUserInformation(Long id, double latitude, double longitude, String interest) {
        validateLocationData(latitude, longitude);
        validateId(id);

        String userKey = "client:" + id;
        String globalKey = "location_data";

        redisTemplate.delete(userKey);
        redisTemplate.opsForZSet().remove(globalKey, id.toString());

        redisTemplate.opsForGeo().add(userKey, new Point(longitude, latitude), id.toString());
        redisTemplate.opsForGeo().add(globalKey, new Point(longitude, latitude), id.toString());

        updateUserInterest(id, interest);

        return true;
    }

    public List<LocationResponse> refreshNearbyUsers(Long id, double latitude, double longitude, String interest) {
        boolean updated = saveUserInformation(id, latitude, longitude, interest);
        return getNearbyUsers(latitude, longitude, interest);
    }

    public List<LocationResponse> getNearbyUsers(double latitude, double longitude, String interest) {
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

        List<LocationResponse> filteredUsers = new ArrayList<>();
        Map<Long, Integer> interestMatchCount = new HashMap<>();

        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results.getContent()) {
            Long targetId = Long.valueOf(result.getContent().getName());

            Map<String, String> targetInterest = getUserInterest(targetId);
            if (!targetInterest.isEmpty()) {
                int matchScore = calculateInterestMatch(interest, targetInterest);
                if (matchScore > 0) {
                    Double distanceValue;
                    if (result.getDistance() != null) {
                        distanceValue = result.getDistance().getValue();
                    } else {
                        distanceValue = 0.0;
                    }

                    filteredUsers.add(new LocationResponse(
                            targetId,
                            result.getContent().getPoint().getY(),
                            result.getContent().getPoint().getX(),
                            distanceValue,
                            targetInterest.toString()
                    ));
                    interestMatchCount.put(targetId, matchScore);
                }
            }
        }

        filteredUsers.sort((u1, u2) -> interestMatchCount.getOrDefault(u2.getId(), 0)
                - interestMatchCount.getOrDefault(u1.getId(), 0));

        if (filteredUsers.size() > 10) {
            return filteredUsers.subList(0, 10);
        } else {
            return filteredUsers;
        }
    }

    private Map<String, String> getUserInterest(Long id) {
        String redisKey = "interest:" + id;

        Map<Object, Object> cachedInterest = redisTemplate.opsForHash().entries(redisKey);
        if (!cachedInterest.isEmpty()) {
            Map<String, String> interestMap = new HashMap<>();
            cachedInterest.forEach((key, value) -> interestMap.put(key.toString(), value.toString()));
            return interestMap;
        }

        String sql = "SELECT topic1, topic2, topic3 FROM it_topic WHERE id = ?";
        Map<String, String> interestMap = jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
            Map<String, String> map = new HashMap<>();
            map.put("topic1", rs.getString("topic1"));
            map.put("topic2", rs.getString("topic2"));
            map.put("topic3", rs.getString("topic3"));
            return map;
        });

        if (interestMap != null) {
            redisTemplate.opsForHash().putAll(redisKey, interestMap);
        }

        return interestMap;
    }

    public void updateUserInterest(Long id, String interest) {
        String[] topicsArray = interest.split(",");
        Set<String> uniqueTopics = new LinkedHashSet<>();

        for (String topic : topicsArray) {
            topic = topic.trim();
            if (!topic.isEmpty()) {
                uniqueTopics.add(topic);
            }
            if (uniqueTopics.size() >= 3) {
                break;
            }
        }

        List<String> topics = new ArrayList<>(uniqueTopics);
        String topic1 = null;
        String topic2 = null;
        String topic3 = null;

        if (topics.size() > 0) {
            topic1 = topics.get(0);
        }
        if (topics.size() > 1) {
            topic2 = topics.get(1);
        }
        if (topics.size() > 2) {
            topic3 = topics.get(2);
        }

        String sql = "UPDATE it_topic SET topic1 = ?, topic2 = ?, topic3 = ? WHERE id = ?";
        jdbcTemplate.update(sql, topic1, topic2, topic3, id);

        String redisKey = "interest:" + id;
        redisTemplate.opsForHash().putAll(redisKey, Map.of("topic1", topic1, "topic2", topic2, "topic3", topic3));
    }

    private void validateLocationData(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_LOCATION_DATA);
        }
    }

    private void validateId(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM client WHERE id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, new Object[]{id}, Boolean.class);
        if (exists == null || !exists) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_USER_ID);
        }
    }

    private int calculateInterestMatch(String userInterest, Map<String, String> targetInterest) {
        int matchScore = 0;
        Set<String> userTopics = new LinkedHashSet<>(Arrays.asList(userInterest.split(",")));

        for (String topic : targetInterest.values()) {
            if (topic != null && userTopics.contains(topic)) {
                matchScore++;
            }
        }

        return matchScore;
    }
}