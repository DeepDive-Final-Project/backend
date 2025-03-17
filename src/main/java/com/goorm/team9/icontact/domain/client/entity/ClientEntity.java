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
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "\"client\"")
public class ClientEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nickName;

    @Column(nullable = false, unique = true)
    private String email;

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

    @Column
    private String link;

    @Column
    private String profileImage;

//    @Column(name = "chat_opportunity", nullable = false)
//    private int chatOpportunity;
//
//    @Column(name = "chat_message")
//    private int chatMessage;
//
//    @Column(nullable = false)
//    private boolean offline;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private TopicEntity it_topic;

    @OneToMany(mappedBy = "client_id", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuth> oauthAccounts = new ArrayList<>(); // 한 User가 여러 OAuth 계정을 가짐

//    public void setChatOpportunity(int chatOpportunity) {
//        if (chatOpportunity < 0 || chatOpportunity > 5) {
//            throw new IllegalArgumentException("채팅 기회는 0~5 사이의 값이어야 합니다.");
//        }
//        this.chatOpportunity = chatOpportunity;
//    }

    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
        if (deleted) {
            super.setDeleted_at(LocalDateTime.now());
        } else {
            super.setDeleted_at(null);
        }
    }

}
