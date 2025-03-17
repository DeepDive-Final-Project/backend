package com.goorm.team9.icontact.domain.block.repository;

import com.goorm.team9.icontact.domain.block.entity.Block;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockRepository extends JpaRepository<Block, Long> {

    void deleteByBlockerAndBlocked(ClientEntity blocker, ClientEntity blocked);

    @Query("SELECT COUNT(b) > 0 FROM Block b WHERE b.blocker = :blocker AND b.blocked = :blocked")
    boolean isUserBlocked(@Param("blocker") ClientEntity blocker, @Param("blocked") ClientEntity blocked);
}
