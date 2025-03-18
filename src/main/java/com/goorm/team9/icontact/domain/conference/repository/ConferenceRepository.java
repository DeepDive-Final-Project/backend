package com.goorm.team9.icontact.domain.conference.repository;

import com.goorm.team9.icontact.domain.conference.entity.ConferenceEntity;
import com.goorm.team9.icontact.domain.conference.enums.Day;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConferenceRepository extends JpaRepository<ConferenceEntity, Long> {
    List<ConferenceEntity> findByDay(Day day);
}
