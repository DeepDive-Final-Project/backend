package com.goorm.team9.icontact.domain.sociallogin.entity;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "oauth", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueEmailProvider", columnNames = {"email", "provider"}) // email + provider 복합 유니크 키
})
public class OAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long oauthId;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true, length = 200)
    private String oauthUserId; // 제공자 내 유저 고유 ID

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = true)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void updateAccessToken(String newAccessToken) {
        this.accessToken = newAccessToken;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRefreshToken(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public OAuth(String oauthUserId, String provider, String accessToken, String refreshToken, String email) {
        this.oauthUserId = oauthUserId;
        this.provider = provider;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
    }

}
