package com.goorm.team9.icontact.domain.conference.entity;

import com.goorm.team9.icontact.domain.conference.enums.Day;
import com.goorm.team9.icontact.domain.lecture.entity.LectureEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conference")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Day day;

    @Builder.Default
    @OneToMany(mappedBy = "conference", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LectureEntity> lectures = new ArrayList<>();
}