package com.goorm.team9.icontact.domain.lecture.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LectureRequestDTO {
    private String title;
    private String lecturer;
    private String openTime;
    private String closeTime;
    private Long conferenceId;
}

