package org.example.workhub.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.workhub.domain.entity.User;

import java.util.List;

@Getter
@AllArgsConstructor
public class CompanyModeratedEvent {

    private List<User> recipients;

    private User sender;

    private String companyName;

    private Long companyId;

    private boolean approved;
}
