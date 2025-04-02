package com.goorm.team9.icontact.domain.location.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationResponseDto {

    private Long id;
    private double latitude;
    private double longitude;
    private String interest;
    private String role;
    private String career;
    private String nickName;
    private String introduction;

    @JsonProperty("distanceToParticipant")
    private String formattedDistance;

    @JsonIgnore
    private transient double distanceValue;

    @JsonIgnore
    private transient int matchScore;

    public LocationResponseDto(Long id, double latitude, double longitude, Double distance, String interest,
                               String role, String career, String nickName, String introduction) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.interest = interest;
        this.role = role;
        this.career = career;
        this.nickName = nickName;
        this.introduction = introduction;

        if (distance != null) {
            this.distanceValue = distance;
            this.formattedDistance = String.format("%.1fm", distance);
        } else {
            this.distanceValue = 0.0;
            this.formattedDistance = "0.0m";
        }
    }
}
