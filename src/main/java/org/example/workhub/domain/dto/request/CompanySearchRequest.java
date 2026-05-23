package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class CompanySearchRequest extends PaginationFullRequestDto {

    @Parameter(description = "Filter by company name")
    private String name;

    @Parameter(description = "Filter by city")
    private String city;

    @Parameter(description = "Filter by country")
    private String country;

    @Parameter(description = "Filter by industry")
    private String industry;

    @Parameter(description = "Filter by company size")
    private String companySize;

    @Parameter(description = "Filter by active flag")
    private Boolean active;

    @Parameter(description = "Filter by verified flag")
    private Boolean verified;

    @Parameter(description = "Created date from, format yyyy-MM-dd'T'HH:mm:ss")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;

    @Parameter(description = "Created date to, format yyyy-MM-dd'T'HH:mm:ss")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;
}
