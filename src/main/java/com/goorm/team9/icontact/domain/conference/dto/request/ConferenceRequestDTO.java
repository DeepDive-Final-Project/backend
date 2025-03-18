package com.goorm.team9.icontact.domain.conference.dto.request;

import com.goorm.team9.icontact.domain.conference.enums.Day;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConferenceRequestDTO {
    private String name;
    private Day day;
}



