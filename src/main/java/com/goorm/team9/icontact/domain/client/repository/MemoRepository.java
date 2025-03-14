package com.goorm.team9.icontact.domain.client.repository;

import com.goorm.team9.icontact.domain.client.entity.MemoEntity;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoRepository extends JpaRepository<MemoEntity, Long> {
    List<MemoEntity> findByWriter(ClientEntity writer);
    List<MemoEntity> findByTarget(ClientEntity target);
}
