package com.goorm.team9.icontact.domain.lecture.repository;

import com.goorm.team9.icontact.domain.lecture.entity.LectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LectureRepository extends JpaRepository<LectureEntity, Long> {

    List<LectureEntity> findByConferenceId(Long conferenceId);

    @Query("SELECT l FROM LectureEntity l JOIN FETCH l.conference WHERE l.conference.id = :conferenceId")
    List<LectureEntity> findByConferenceIdWithConference(@Param("conferenceId") Long conferenceId);

    @Query("SELECT l FROM LectureEntity l JOIN FETCH l.conference WHERE l.id = :id")
    Optional<LectureEntity> findByIdWithConference(@Param("id") Long id);

}
