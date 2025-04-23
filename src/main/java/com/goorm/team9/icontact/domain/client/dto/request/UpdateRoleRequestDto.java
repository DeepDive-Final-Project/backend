package com.goorm.team9.icontact.domain.client.dto.request;

import com.goorm.team9.icontact.domain.client.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequestDto {

    private Role role;

}
