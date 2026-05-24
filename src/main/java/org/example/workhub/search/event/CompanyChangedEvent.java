package org.example.workhub.search.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompanyChangedEvent {

    private Long companyId;
}
