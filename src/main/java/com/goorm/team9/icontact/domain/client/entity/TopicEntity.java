package com.goorm.team9.icontact.domain.client.entity;

import com.goorm.team9.icontact.domain.client.enums.Framework;
import com.goorm.team9.icontact.domain.client.enums.Interest;
import com.goorm.team9.icontact.domain.client.enums.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "it_topic")
public class TopicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Interest topic1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Interest topic2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Interest topic3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Framework framework;
}
