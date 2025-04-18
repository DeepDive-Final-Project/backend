package com.goorm.team9.icontact.domain.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestCountDto {

    private long receivedCount;
    private long sentCount;

}
