package com.goorm.team9.icontact.api.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {
    private String userId;
    private double latitude;
    private double longitude;
    private String interest;
}
