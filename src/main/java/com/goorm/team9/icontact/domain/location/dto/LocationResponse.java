package com.goorm.team9.icontact.domain.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationResponse {
    private Long id;
    private double latitude;
    private double longitude;
    private String interest;

    @JsonProperty("distanceToParticipant")
    private String formattedDistance;

    @JsonIgnore
    private transient double distanceValue;

    public LocationResponse(Long id, double latitude, double longitude, Double distance, String interest) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.interest = interest;

        if (distance != null) {
            this.distanceValue = distance;
            this.formattedDistance = String.format("%.1fm", distance);
        } else {
            this.distanceValue = 0.0;
            this.formattedDistance = "0.0m";
        }
    }
}