package org.example.workhub.search.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobSuggestionResponse {

    private String text;

    private String type;

    private Long jobId;

    private Long skillId;

    private Long companyId;
}
