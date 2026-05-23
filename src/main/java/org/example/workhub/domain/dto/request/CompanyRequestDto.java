package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.ErrorMessage;

@Getter
@Setter
@Schema(description = "Company create/update request")
public class CompanyRequestDto {

    @NotBlank(message = "{" + ErrorMessage.NOT_BLANK_FIELD + "}")
    @Size(max = 255, message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD + "}")
    @Schema(description = "Company name", example = "WorkHub Technologies")
    private String name;

    @Schema(description = "Company description")
    private String description;

    @Pattern(regexp = "^(https?://).+", message = "{" + ErrorMessage.Company.ERR_INVALID_WEBSITE + "}")
    @Schema(description = "Company website", example = "https://workhub.vn")
    private String website;

    @Email(message = "{" + ErrorMessage.Company.ERR_INVALID_EMAIL + "}")
    @Schema(description = "Company contact email", example = "hr@workhub.vn")
    private String email;

    @Pattern(regexp = "^[0-9+()\\-\\s]{8,20}$", message = "{" + ErrorMessage.Company.ERR_INVALID_PHONE + "}")
    @Schema(description = "Company contact phone", example = "+84901234567")
    private String phone;

    @Schema(description = "Company address", example = "District 1")
    private String address;

    @Schema(description = "Company city", example = "Ho Chi Minh City")
    private String city;

    @Schema(description = "Company country", example = "Vietnam")
    private String country;

    @Schema(description = "Company size", example = "51-200")
    private String companySize;

    @Schema(description = "Company industry", example = "Software")
    private String industry;

    @Schema(description = "Company tax code", example = "0312345678")
    private String taxCode;

    @Schema(description = "Logo URL. Prefer using upload logo API for files.")
    private String logo;

    @Schema(description = "Cover image URL. Prefer using upload cover API for files.")
    private String coverImage;

    @Schema(description = "Company active flag", example = "true")
    private Boolean active;
}
