package org.example.workhub.domain.dto.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class UserAuditingDto {

  private String createdBy;

  private String lastModifiedBy;

}
