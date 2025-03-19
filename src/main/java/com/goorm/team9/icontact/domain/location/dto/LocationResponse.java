package com.goorm.team9.icontact.domain.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationResponse {
    private Long id;
    private double latitude;
    private double longitude;
    private double distance;
    private String interest;

    public LocationResponse(Long id, double latitude, double longitude, Double distance, String interest) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;

        if (distance != null) {
            this.distance = distance;
        } else {
            this.distance = 0.0;
        }

        this.interest = interest;
    }

    public double getDistanceValue() {
        return this.distance;
    }

    public String getFormattedDistance() {
        return String.format("%.1fm", this.distance);
    }
}
