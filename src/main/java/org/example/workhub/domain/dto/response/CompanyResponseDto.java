package org.example.workhub.domain.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CompanyResponseDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String logo;
}
