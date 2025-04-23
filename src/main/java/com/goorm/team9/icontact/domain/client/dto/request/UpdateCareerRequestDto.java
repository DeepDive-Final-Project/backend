package com.goorm.team9.icontact.domain.client.dto.request;

import com.goorm.team9.icontact.domain.client.enums.Career;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCareerRequestDto {

    private Career career;

}
