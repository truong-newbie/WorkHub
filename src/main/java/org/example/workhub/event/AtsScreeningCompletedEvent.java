package org.example.workhub.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.workhub.domain.entity.User;

@Getter
@AllArgsConstructor
public class AtsScreeningCompletedEvent {

    private User recipient;

    private String candidateName;

    private String screeningResultId;
}
