package com.goorm.team9.icontact.api.location.service;

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

    public boolean saveLocation(Long id, double latitude, double longitude, String interest) {
        validateLocationData(latitude, longitude);
        validateId(id);

        String userKey = "client:" + id;
        String globalKey = "location_data";

        redisTemplate.delete(userKey);
        redisTemplate.opsForZSet().remove(globalKey, id.toString());

        redisTemplate.opsForGeo().add(userKey, new Point(longitude, latitude), id.toString());
        redisTemplate.opsForGeo().add(globalKey, new Point(longitude, latitude), id.toString());

        saveUserInterest(id, interest);
        return true;
    }

    public List<LocationResponse> refreshNearbyUsers(Long id, double latitude, double longitude) {
        boolean updated = saveLocation(id, latitude, longitude, getUserInterestAsString(id));
        return getNearbyUsers(latitude, longitude, getUserInterestAsString(id));
    }

    public List<LocationResponse> getNearbyUsers(double latitude, double longitude, String interest) {
        String key = "location_data";
        double radius = 10;

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(
                        key,
                        new Circle(new Point(longitude, latitude), new Distance(radius, RedisGeoCommands.DistanceUnit.METERS)),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().includeDistance()
                );

        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        List<LocationResponse> filteredUsers = new ArrayList<>();
        Map<Long, Integer> interestMatchCount = new HashMap<>();

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> resultList = results.getContent();

        for (int i = 0; i < resultList.size(); i++) {
            GeoResult<RedisGeoCommands.GeoLocation<String>> result = resultList.get(i);
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

                    filteredUsers.add(new LocationResponse(targetId,
                            result.getContent().getPoint().getY(),
                            result.getContent().getPoint().getX(),
                            distanceValue,
                            targetInterest.toString()));
                    interestMatchCount.put(targetId, matchScore);
                }
            }
        }

        filteredUsers.sort((u1, u2) -> interestMatchCount.getOrDefault(u2.getId(), 0) -
                interestMatchCount.getOrDefault(u1.getId(), 0));

        if (filteredUsers.size() > 10) {
            List<LocationResponse> topMatches = new ArrayList<>();
            int highestMatch = interestMatchCount.getOrDefault(filteredUsers.get(0).getId(), 0);

            for (int i = 0; i < filteredUsers.size(); i++) {
                LocationResponse user = filteredUsers.get(i);
                if (interestMatchCount.getOrDefault(user.getId(), 0) == highestMatch) {
                    topMatches.add(user);
                }
            }

            Collections.shuffle(topMatches);

            int limit = Math.min(10, topMatches.size());
            List<LocationResponse> result = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                result.add(topMatches.get(i));
            }

            return result;
        }

        return filteredUsers;
    }

    private void saveUserInterest(Long id, String interest) {
        String[] topicsArray = interest.split(",");
        Set<String> uniqueTopics = new LinkedHashSet<>();

        for (int i = 0; i < topicsArray.length; i++) {
            String topic = topicsArray[i].trim();
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

        for (int i = 0; i < topics.size(); i++) {
            if (i == 0) {
                topic1 = topics.get(i);
            } else if (i == 1) {
                topic2 = topics.get(i);
            } else if (i == 2) {
                topic3 = topics.get(i);
            }
        }

        String sql = "INSERT INTO it_topic (client_id, topic1, topic2, topic3) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE topic1 = VALUES(topic1), topic2 = VALUES(topic2), topic3 = VALUES(topic3)";
        jdbcTemplate.update(sql, id, topic1, topic2, topic3);
    }

    private String getUserInterestAsString(Long id) {
        Map<String, String> interestMap = getUserInterest(id);
        Set<String> interestSet = new LinkedHashSet<>();

        List<String> topicList = new ArrayList<>(interestMap.values());

        for (int i = 0; i < topicList.size(); i++) {
            String topic = topicList.get(i);
            if (topic != null && !topic.isEmpty()) {
                interestSet.add(topic);
            }
        }

        return String.join(",", interestSet);
    }

    private Map<String, String> getUserInterest(Long id) {
        String sql = "SELECT topic1, topic2, topic3 FROM it_topic WHERE client_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
            Map<String, String> interestMap = new HashMap<>();
            interestMap.put("topic1", rs.getString("topic1"));
            interestMap.put("topic2", rs.getString("topic2"));
            interestMap.put("topic3", rs.getString("topic3"));
            return interestMap;
        });
    }

    private int calculateInterestMatch(String userInterest, Map<String, String> targetInterest) {
        int matchScore = 0;
        Set<String> topicSet = new LinkedHashSet<>(Arrays.asList(userInterest.split(",")));

        List<String> topicList = new ArrayList<>(targetInterest.values());

        for (int i = 0; i < topicList.size(); i++) {
            String topic = topicList.get(i);
            if (topic != null && topicSet.contains(topic)) {
                matchScore++;
            }
        }

        return matchScore;
    }

    private void validateLocationData(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_LOCATION_DATA);
        }
        if (latitude == 0.0 && longitude == 0.0) {
            throw new CustomException(GlobalExceptionErrorCode.GPS_ERROR);
        }
    }

    private void validateId(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM client WHERE client_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, new Object[]{id}, Boolean.class);
        if (exists == null || !exists) {
            throw new CustomException(GlobalExceptionErrorCode.INVALID_USER_ID);
        }
    }
}