package com.goorm.team9.icontact.domain.client.dto.request;

import com.goorm.team9.icontact.domain.client.enums.Interest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInterestRequestDto {

    private Interest topic1;
    private Interest topic2;
    private Interest topic3;

}
