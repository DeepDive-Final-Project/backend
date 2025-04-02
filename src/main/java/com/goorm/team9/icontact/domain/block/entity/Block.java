package com.goorm.team9.icontact.domain.block.entity;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private ClientEntity blocker;

    @ManyToOne
    @JoinColumn(name = "blocked_id", nullable = false)
    private ClientEntity blocked;

    public static Block create(ClientEntity blocker, ClientEntity blocked) {
        return Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
    }

}
