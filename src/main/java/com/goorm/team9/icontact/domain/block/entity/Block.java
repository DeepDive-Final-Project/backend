package com.goorm.team9.icontact.domain.block.entity;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "block")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "blocker_id", nullable = false)
    private ClientEntity blocker; // 차단한 사용자

    @ManyToOne
    @JoinColumn(name = "blocked_id", nullable = false)
    private ClientEntity blocked; // 차단당한 사용자

    public static Block create(ClientEntity blocker, ClientEntity blocked) {
        return Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
    }
}
