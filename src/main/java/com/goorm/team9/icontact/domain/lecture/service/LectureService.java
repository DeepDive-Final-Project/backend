package com.goorm.team9.icontact.domain.lecture.service;

import com.goorm.team9.icontact.common.error.LectureErrorCode;
import com.goorm.team9.icontact.common.exception.CustomException;
import com.goorm.team9.icontact.domain.conference.entity.ConferenceEntity;
import com.goorm.team9.icontact.domain.conference.repository.ConferenceRepository;
import com.goorm.team9.icontact.domain.lecture.dto.request.LectureRequestDTO;
import com.goorm.team9.icontact.domain.lecture.dto.response.LectureResponseDTO;
import com.goorm.team9.icontact.domain.lecture.entity.LectureEntity;
import com.goorm.team9.icontact.domain.lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final ConferenceRepository conferenceRepository;

    public LectureResponseDTO createLecture(String title, String lecturer, String openTime, String closeTime, Long conferenceId) {
        LocalTime open = LocalTime.parse(openTime);
        LocalTime close = LocalTime.parse(closeTime);

        ConferenceEntity conference = conferenceRepository.findById(conferenceId)
                .orElseThrow(() -> new CustomException(LectureErrorCode.CONFERENCE_NOT_FOUND));

        LectureEntity lecture = LectureEntity.builder()
                .title(title)
                .lecturer(lecturer)
                .openTime(open)
                .closeTime(close)
                .conference(conference)
                .build();

        lectureRepository.save(lecture);
        return new LectureResponseDTO(lecture);
    }


    public List<LectureResponseDTO> getLecturesByConference(Long conferenceId) {
        List<LectureEntity> lectures = lectureRepository.findByConferenceId(conferenceId);
        return lectures.stream()
                .map(lec -> new LectureResponseDTO(
                        lec.getId(),
                        lec.getTitle(),
                        lec.getLecturer(),
                        lec.getOpenTime().toString(),
                        lec.getCloseTime().toString(),
                        lec.getConference().getName(),
                        lec.getConference().getDay().getDescription()
                ))
                .collect(Collectors.toList());
    }
}

