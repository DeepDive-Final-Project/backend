package com.goorm.team9.icontact.api.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationResponse {
    private String userId;
    private double latitude;
    private double longitude;
    private double distance;
}
