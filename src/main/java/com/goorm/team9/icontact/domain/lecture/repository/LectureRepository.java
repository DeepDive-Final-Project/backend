package com.goorm.team9.icontact.domain.lecture.repository;

import com.goorm.team9.icontact.domain.lecture.entity.LectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureRepository extends JpaRepository<LectureEntity, Long> {

    List<LectureEntity> findByConferenceId(Long conferenceId);
}
