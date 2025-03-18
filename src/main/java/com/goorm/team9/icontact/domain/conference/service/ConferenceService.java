package com.goorm.team9.icontact.domain.conference.service;

import com.goorm.team9.icontact.domain.conference.dto.request.ConferenceRequestDTO;
import com.goorm.team9.icontact.domain.conference.dto.response.ConferenceResponseDTO;
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

    public ConferenceResponseDTO createConference(String name, Day day) {
        ConferenceEntity conference = ConferenceEntity.builder()
                .name(name)
                .day(day)
                .build();

        conferenceRepository.save(conference);
        return new ConferenceResponseDTO(conference);
    }

    public List<ConferenceResponseDTO> getConferencesByDay(Day day) {
        return conferenceRepository.findByDay(day).stream()
                .map(conf -> new ConferenceResponseDTO(conf.getId(), conf.getName(), conf.getDay().getDescription()))
                .collect(Collectors.toList());
    }
}
