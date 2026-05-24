package org.example.workhub.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.workhub.constant.StatusEnum;
import org.example.workhub.domain.entity.User;

@Getter
@AllArgsConstructor
public class JobApplicationStatusUpdatedEvent {

    private User recipient;

    private User sender;

    private String jobTitle;

    private StatusEnum status;

    private Long applicationId;
}
