package org.example.workhub.domain.dto.pagination;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.CommonConstant;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PaginationFullRequestDto extends PaginationSortRequestDto {

  @Parameter(description = "Keyword to search")
  private String keyword = CommonConstant.EMPTY_STRING;

  public PaginationFullRequestDto(Integer pageNum, Integer pageSize, String sortBy, boolean isAscending, String keyword) {
    super(pageNum, pageSize, sortBy, isAscending);
    this.keyword = keyword;
  }

  public String getKeyword() {
    return keyword.trim();
  }

}
