package com.goorm.team9.icontact.domain.client.service;

import com.goorm.team9.icontact.common.exception.CustomException;
import com.goorm.team9.icontact.common.error.MemoErrorCode;
import com.goorm.team9.icontact.domain.client.dto.response.MemoResponseDTO;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.entity.MemoEntity;
import com.goorm.team9.icontact.domain.client.repository.MemoRepository;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoService {

    private final MemoRepository memoRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public MemoResponseDTO createMemo(Long writerId, Long targetId, String content) {
        ClientEntity writer = clientRepository.findById(writerId)
                .orElseThrow(() -> new CustomException(MemoErrorCode.MEMO_WRITER_NOT_FOUND));

        ClientEntity target = clientRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(MemoErrorCode.MEMO_TARGET_NOT_FOUND));

        MemoEntity memo = MemoEntity.builder()
                .writer(writer)
                .target(target)
                .content(content)
                .build();

        memoRepository.save(memo);
        return new MemoResponseDTO(memo);
    }

    @Transactional(readOnly = true)
    public List<MemoResponseDTO> getMemosByWriter(Long writerId) {
        ClientEntity writer = clientRepository.findById(writerId)
                .orElseThrow(() -> new CustomException(MemoErrorCode.MEMO_WRITER_NOT_FOUND));

        return memoRepository.findByWriter(writer).stream()
                .map(MemoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMemo(Long memoId) {
        if (!memoRepository.existsById(memoId)) {
            throw new CustomException(MemoErrorCode.MEMO_NOT_FOUND);
        }
        memoRepository.deleteById(memoId);
    }
}
