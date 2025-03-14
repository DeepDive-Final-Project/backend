package com.goorm.team9.icontact.domain.client.dto.response;

import com.goorm.team9.icontact.domain.client.entity.MemoEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemoResponseDTO {
    private Long id;
    private Long targetId;
    private String content;

    public MemoResponseDTO(MemoEntity memo) {
        this.id = memo.getId();
        this.targetId = memo.getTarget().getId();
        this.content = memo.getContent();
    }
}
