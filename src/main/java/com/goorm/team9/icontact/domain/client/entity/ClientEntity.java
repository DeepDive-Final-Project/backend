package com.goorm.team9.icontact.domain.client.entity;

import com.goorm.team9.icontact.domain.client.enums.Career;
import com.goorm.team9.icontact.domain.client.enums.Industry;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.enums.Status;
import com.goorm.team9.icontact.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "\"client\"")
public class ClientEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nickName;

    @Column(nullable = false)
    private Long age;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column
    private Industry industry;

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

    @Column(name = "chat_opportunity")
    private int chatOpportunity;

    @Column(name = "chat_message")
    private int chatMessage;

    @Column(nullable = false)
    private boolean offline;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private TopicEntity it_topic;

}
