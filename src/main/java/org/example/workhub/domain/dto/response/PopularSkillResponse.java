package org.example.workhub.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Popular skill response")
public class PopularSkillResponse {
    private Long id;
    private String name;
    private String slug;
    private String level;
    private Long usageCount;
}
