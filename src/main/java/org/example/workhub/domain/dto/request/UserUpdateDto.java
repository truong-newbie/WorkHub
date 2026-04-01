package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.GenderEnum;
import org.example.workhub.domain.entity.Role;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserUpdateDto {

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String id;

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String username;

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String age;

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String address;

  @NotNull(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
  private GenderEnum gender;

  private Role role;

}
