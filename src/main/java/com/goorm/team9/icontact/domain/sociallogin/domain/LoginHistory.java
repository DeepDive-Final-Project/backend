package com.goorm.team9.icontact.domain.sociallogin.domain;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private ClientEntity clientEntity;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private LocalDateTime loginAt;
}