package com.goorm.team9.icontact.domain.conference.dto.response;

import com.goorm.team9.icontact.domain.conference.entity.ConferenceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConferenceResponseDto {

    private Long id;
    private String name;
    private String day;

    public ConferenceResponseDto(ConferenceEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.day = entity.getDay().getDescription();
    }

}
