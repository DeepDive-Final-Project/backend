package com.goorm.team9.icontact.domain.client.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;
}
