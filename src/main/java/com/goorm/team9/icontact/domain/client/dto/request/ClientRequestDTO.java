package com.goorm.team9.icontact.domain.client.dto.request;

import com.goorm.team9.icontact.domain.client.enums.Career;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.enums.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRequestDTO {

    @NotNull
    @Size(min = 2, max = 30)
    private String nickName;

    @NotNull
    private Long age;

    @NotNull
    private String email;

    private Role role;
    private Career career;
    private Status status;

    @Size(max = 200)
    private String introduction;

    private String link;
    private String profileImage;
    private int chat_opportunity;
    private int chat_message;
    private boolean offline;
}
