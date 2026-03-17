package org.example.workhub.domain.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.domain.dto.common.DateAuditingDto;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto extends DateAuditingDto {

  private String id;

  private String username;

  private String firstName;

  private String lastName;

  private String roleName;

}

