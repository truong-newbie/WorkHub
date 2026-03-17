package org.example.workhub.domain.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.workhub.constant.ErrorMessage;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TokenRefreshRequestDto {

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String refreshToken;

}
