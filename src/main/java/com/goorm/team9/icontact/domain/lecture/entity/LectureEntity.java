package com.goorm.team9.icontact.domain.lecture.entity;

import com.goorm.team9.icontact.domain.conference.entity.ConferenceEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "lecture")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String lecturer;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_id")
    private ConferenceEntity conference;
}

