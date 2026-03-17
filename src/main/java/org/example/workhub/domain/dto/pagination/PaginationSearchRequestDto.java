package org.example.workhub.domain.dto.pagination;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.CommonConstant;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class PaginationSearchRequestDto extends PaginationRequestDto {

  @Parameter(description = "Keyword to search")
  private String keyword = CommonConstant.EMPTY_STRING;

  public String getKeyword() {
    return keyword.trim();
  }

}
