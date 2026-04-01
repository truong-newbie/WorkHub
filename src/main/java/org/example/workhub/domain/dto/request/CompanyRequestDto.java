package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyRequestDto {

    @NotBlank(message = "Ten cong ty khong duoc de trong")
    private String name;

    private String description;
    private String address;
    private String logo;
}
