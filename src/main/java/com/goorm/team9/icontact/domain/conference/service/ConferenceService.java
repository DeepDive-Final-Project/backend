package com.goorm.team9.icontact.domain.conference.service;

import com.goorm.team9.icontact.common.error.ConferenceErrorCode;
import com.goorm.team9.icontact.common.exception.CustomException;
import com.goorm.team9.icontact.domain.conference.dto.response.ConferenceResponseDto;
import com.goorm.team9.icontact.domain.conference.entity.ConferenceEntity;
import com.goorm.team9.icontact.domain.conference.enums.Day;
import com.goorm.team9.icontact.domain.conference.repository.ConferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConferenceService {

    private final ConferenceRepository conferenceRepository;

    public ConferenceResponseDto createConference(String name, Day day) {
        ConferenceEntity conference = ConferenceEntity.builder()
                .name(name)
                .day(day)
                .build();

        conferenceRepository.save(conference);
        return new ConferenceResponseDto(conference);
    }

    public List<ConferenceResponseDto> getConferencesByDay(Day day) {
        return conferenceRepository.findByDay(day).stream()
                .map(conf -> new ConferenceResponseDto(conf.getId(), conf.getName(), conf.getDay().getDescription()))
                .collect(Collectors.toList());
    }

    public ConferenceResponseDto updateConference(Long id, String name, Day day) {
        ConferenceEntity conference = conferenceRepository.findById(id)
                .orElseThrow(() -> new CustomException(ConferenceErrorCode.CONFERENCE_NOT_FOUND));

        if (name != null && !name.isBlank()) {
            conference.setName(name);
        }

        if (day != null) {
            conference.setDay(day);
        }

        conferenceRepository.save(conference);
        return new ConferenceResponseDto(conference);
    }

    public void deleteConference(Long id) {
        ConferenceEntity conference = conferenceRepository.findById(id)
                .orElseThrow(() -> new CustomException(ConferenceErrorCode.CONFERENCE_NOT_FOUND));
        conferenceRepository.delete(conference);
    }

}
