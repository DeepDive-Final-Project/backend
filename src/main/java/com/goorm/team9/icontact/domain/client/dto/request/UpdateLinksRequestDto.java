package com.goorm.team9.icontact.domain.client.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class UpdateLinksRequestDto {

    private List<ClientLinkRequestDto> links;

}
