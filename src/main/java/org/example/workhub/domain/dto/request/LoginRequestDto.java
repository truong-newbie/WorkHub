package org.example.workhub.domain.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.example.workhub.constant.ErrorMessage;



@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoginRequestDto {

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String email;

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String password;

  private String fullname;

  private String profileImage;

  private String googleAccountId;

  private String facebookAccountId;

}
