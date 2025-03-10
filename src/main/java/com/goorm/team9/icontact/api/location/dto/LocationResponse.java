package com.goorm.team9.icontact.api.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationResponse {
    private String userId;
    private double latitude;
    private double longitude;
    private String distance;
    private String interest;

    public LocationResponse(String userId, double latitude, double longitude, Double distance, String interest) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;

        if (distance != null) {
            this.distance = String.format("%.1fm", distance);
        } else {
            this.distance = "0.0m";
        }

        this.interest = interest;
    }
}
