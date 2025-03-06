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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.geo.search-radius}")
    private double searchRadius;

    public void saveLocation(LocationRequest request) {
        validateLocationData(request);

        String key = "offline_users";
        redisTemplate.opsForGeo().add(key, new Point(request.getLongitude(), request.getLatitude()), request.getUserId());
    }

    public List<LocationResponse> getNearbyUsers(double latitude, double longitude) {
        String key = "offline_users";
        double radius = searchRadius;

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(
                        key,
                        new Circle(new Point(longitude, latitude), new Distance(radius, RedisGeoCommands.DistanceUnit.METERS)),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().includeDistance()
                );

        return results == null || results.getContent().isEmpty()
                ? Collections.emptyList()
                : results.getContent().stream()
                .map(result -> new LocationResponse(
                        result.getContent().getName(),
                        result.getContent().getPoint().getY(),
                        result.getContent().getPoint().getX(),
                        Optional.ofNullable(result.getDistance()).map(Distance::getValue).orElse(0.0)
                ))
                .collect(Collectors.toList());
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
}
