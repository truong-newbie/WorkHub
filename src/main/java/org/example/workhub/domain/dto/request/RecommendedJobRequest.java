package org.example.workhub.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendedJobRequest {

    private int pageNum = 1;

    private int pageSize = 10;

    private String location;

    private Boolean refresh = false;

    private Boolean explain = false;
}
