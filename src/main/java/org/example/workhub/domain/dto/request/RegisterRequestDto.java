package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.GenderEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Email(message = ErrorMessage.INVALID_EMAIL)
    private String email;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Pattern(regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{6,}$", message = ErrorMessage.INVALID_PASSWORD)
    private String password;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String username;

    @NotNull
    private LocalDate dob;

    private GenderEnum gender;


}
