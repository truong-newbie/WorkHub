package org.example.workhub.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.RecommendationReasonCode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationReasonResponse {

    private RecommendationReasonCode code;

    private String text;
}
