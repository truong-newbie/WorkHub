package org.example.workhub.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobBehaviorTrackRequest {

    private String source;

    private String sessionId;

    private Integer position;
}
