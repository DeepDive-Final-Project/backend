package com.goorm.team9.icontact.domain.lecture.dto.response;

import com.goorm.team9.icontact.domain.lecture.entity.LectureEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LectureResponseDTO {
    private Long id;
    private String title;
    private String lecturer;
    private String openTime;
    private String closeTime;
    private String conferenceName;
    private String conferenceDay;

    public LectureResponseDTO(LectureEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.lecturer = entity.getLecturer();
        this.openTime = entity.getOpenTime().toString();
        this.closeTime = entity.getCloseTime().toString();
        this.conferenceName = entity.getConference().getName();
        this.conferenceDay = entity.getConference().getDay().getDescription();
    }

}


