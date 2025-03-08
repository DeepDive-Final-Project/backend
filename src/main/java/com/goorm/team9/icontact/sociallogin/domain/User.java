package com.goorm.team9.icontact.sociallogin.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Long age;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Industry industry;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Career career;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(length = 200)
    private String introduction;

    @Column
    private String link;

    @Lob
    private String profileImage;

    @Column
    private Long chatOpportunity;

    @Column
    private Long chatMessage;

    @Column
    private boolean offline;

    @Column
    private boolean isDeleted;

    @Column
    private LocalDateTime deletedAt;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "oauth_id")
//    private OAuth oauth;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuth> oauthAccounts = new ArrayList<>(); // 한 User가 여러 OAuth 계정을 가짐
}