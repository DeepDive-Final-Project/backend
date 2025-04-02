package com.goorm.team9.icontact.domain.lecture.service;

import com.goorm.team9.icontact.common.error.LectureErrorCode;
import com.goorm.team9.icontact.common.exception.CustomException;
import com.goorm.team9.icontact.domain.conference.entity.ConferenceEntity;
import com.goorm.team9.icontact.domain.conference.repository.ConferenceRepository;
import com.goorm.team9.icontact.domain.lecture.dto.response.LectureResponseDto;
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

    public LectureResponseDto createLecture(String title, String lecturer, String openTime, String closeTime, Long conferenceId) {
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
        return new LectureResponseDto(lecture);
    }

    public List<LectureResponseDto> getLecturesByConference(Long conferenceId) {
        List<LectureEntity> lectures = lectureRepository.findByConferenceIdWithConference(conferenceId);
        return lectures.stream()
                .map(LectureResponseDto::new)
                .collect(Collectors.toList());
    }

    public LectureResponseDto updateLecture(Long id, String title, String lecturer, String openTime, String closeTime) {
        LectureEntity lecture = lectureRepository.findByIdWithConference(id)
                .orElseThrow(() -> new CustomException(LectureErrorCode.LECTURE_NOT_FOUND));

        if (title != null && !title.isBlank()) lecture.setTitle(title);
        if (lecturer != null && !lecturer.isBlank()) lecture.setLecturer(lecturer);
        if (openTime != null && !openTime.isBlank()) lecture.setOpenTime(LocalTime.parse(openTime));
        if (closeTime != null && !closeTime.isBlank()) lecture.setCloseTime(LocalTime.parse(closeTime));

        lectureRepository.save(lecture);
        return new LectureResponseDto(lecture);
    }

    public void deleteLecture(Long id) {
        LectureEntity lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new CustomException(LectureErrorCode.LECTURE_NOT_FOUND));
        lectureRepository.delete(lecture);
    }

}

