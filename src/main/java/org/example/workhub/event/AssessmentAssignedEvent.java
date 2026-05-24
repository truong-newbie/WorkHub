package org.example.workhub.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.workhub.domain.entity.User;

@Getter
@AllArgsConstructor
public class AssessmentAssignedEvent {

    private User recipient;

    private User sender;

    private String jobTitle;

    private Long assignmentId;
}
