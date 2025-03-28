package com.goorm.team9.icontact.domain.client.entity;

import com.goorm.team9.icontact.domain.client.enums.Career;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.enums.Status;
import com.goorm.team9.icontact.domain.common.BaseTimeEntity;
import com.goorm.team9.icontact.domain.sociallogin.entity.OAuth;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "client", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueEmailProvider", columnNames = {"email", "provider"}) // ✅ email + provider 조합이 유니크하도록 설정
})
public class ClientEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nickName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column
    private Career career;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(length = 200)
    private String introduction;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientLinkEntity> links = new ArrayList<>();

    @Column
    private String profileImage;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private TopicEntity it_topic;

    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuth> oauthAccounts = new ArrayList<>(); // 한 User가 여러 OAuth 계정을 가짐

    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
        if (deleted) {
            super.setDeleted_at(LocalDateTime.now());
        } else {
            super.setDeleted_at(null);
        }
    }

    @PostLoad
    private void initCollections() {
        if (this.oauthAccounts == null) {
            this.oauthAccounts = new ArrayList<>();
        }
    }

}
