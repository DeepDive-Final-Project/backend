package com.goorm.team9.icontact.sociallogin.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long oauthId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true, length = 200)
    private String oauthUserId; // 제공자 내 유저 고유 ID

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
