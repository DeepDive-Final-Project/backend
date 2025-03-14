package com.goorm.team9.icontact.domain.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationResponse {
    private Long id;
    private double latitude;
    private double longitude;
    private String distance;
    private String interest;

    public LocationResponse(Long id, double latitude, double longitude, Double distance, String interest) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = (distance != null) ? String.format("%.1fm", distance) : "0.0m";
        this.interest = interest;
    }
}