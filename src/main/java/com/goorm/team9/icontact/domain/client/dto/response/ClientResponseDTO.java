package com.goorm.team9.icontact.domain.client.dto.response;

import com.goorm.team9.icontact.domain.client.enums.Career;
import com.goorm.team9.icontact.domain.client.enums.Industry;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ClientResponseDTO {

    private Long id;
    private String nickName;
    private Long age;
    private String email;
    private Industry industry;
    private Role role;
    private Career career;
    private Status status;
    private String introduction;
    private String link;
    private String profileImage;
    private int chatOpportunity;
    private int chatMessage;
    private boolean offline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
