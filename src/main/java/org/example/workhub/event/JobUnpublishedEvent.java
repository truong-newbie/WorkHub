package org.example.workhub.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobUnpublishedEvent {

    private Long jobId;
}
