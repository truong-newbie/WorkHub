package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class SkillSearchRequest extends PaginationFullRequestDto {

    @Parameter(description = "Filter by skill name")
    private String name;

    @Parameter(description = "Filter by skill slug")
    private String slug;

    @Parameter(description = "Filter by skill level")
    private String level;

    @Parameter(description = "Filter by active status")
    private Boolean active;

    @Parameter(description = "Filter by deleted status. Admin only should use true.")
    private Boolean deleted;

    @Parameter(description = "Created date from, format yyyy-MM-dd'T'HH:mm:ss")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;

    @Parameter(description = "Created date to, format yyyy-MM-dd'T'HH:mm:ss")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;
}
