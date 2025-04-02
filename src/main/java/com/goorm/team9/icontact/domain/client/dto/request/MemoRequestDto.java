package com.goorm.team9.icontact.domain.client.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemoRequestDto {

    private Long targetId;
    private String content;

}
