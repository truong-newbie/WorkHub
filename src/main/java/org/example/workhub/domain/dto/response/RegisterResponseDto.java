package org.example.workhub.domain.dto.response;

import lombok.*;
import org.example.workhub.constant.GenderEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponseDto {

    private String email;
    private String username;
    private String password;
    private GenderEnum gender;

}
