package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Company response")
public class CompanyResponseDto {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String website;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String companySize;
    private String industry;
    private String taxCode;
    private Boolean active;
    private Boolean verified;
    private Boolean deleted;
    private String logo;
    private String coverImage;
    private OwnerSummary owner;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OwnerSummary {
        private String id;
        private String username;
        private String email;
    }
}
