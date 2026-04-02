package org.example.workhub.domain.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SkillResponseDto {
    private Long id;
    private String name;
}
