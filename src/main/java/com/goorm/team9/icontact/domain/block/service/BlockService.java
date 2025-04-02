package com.goorm.team9.icontact.domain.block.service;

import com.goorm.team9.icontact.domain.block.entity.Block;
import com.goorm.team9.icontact.domain.block.repository.BlockRepository;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public void blockUser(String blockerNickname, String blockedNickname) {
        ClientEntity blocker = clientRepository.findByNickName(blockerNickname)
                .orElseThrow(() -> new IllegalArgumentException("차단하는 사용자를 찾을 수 없습니다."));
        ClientEntity blocked = clientRepository.findByNickName(blockedNickname)
                .orElseThrow(() -> new IllegalArgumentException("차단당할 사용자를 찾을 수 없습니다."));

        if (blockRepository.isUserBlocked(blocker, blocked)) {
            throw new IllegalArgumentException("이미 차단된 사용자입니다.");
        }

        blockRepository.save(Block.create(blocker, blocked));
    }

    @Transactional
    public void unblockUser(String blockerNickname, String blockedNickname) {
        ClientEntity blocker = clientRepository.findByNickName(blockerNickname)
                .orElseThrow(() -> new IllegalArgumentException("차단을 해제할 사용자를 찾을 수 없습니다."));
        ClientEntity blocked = clientRepository.findByNickName(blockedNickname)
                .orElseThrow(() -> new IllegalArgumentException("차단된 사용자를 찾을 수 없습니다."));

        blockRepository.deleteByBlockerAndBlocked(blocker, blocked);
    }

}