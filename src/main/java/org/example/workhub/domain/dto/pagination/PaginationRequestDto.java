package org.example.workhub.domain.dto.pagination;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.CommonConstant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaginationRequestDto {

  @Parameter(description = "Page you want to retrieve (0..N)")
  private Integer pageNum = CommonConstant.ZERO_INT_VALUE;

  @Parameter(description = "Number of records per page")
  private Integer pageSize = CommonConstant.ZERO_INT_VALUE;

  public int getPageNum() {
    if (pageNum < 1) {
      pageNum = CommonConstant.ONE_INT_VALUE;
    }
    return pageNum - 1;
  }

  public int getPageSize() {
    if (pageSize < 1) {
      pageSize = CommonConstant.PAGE_SIZE_DEFAULT;
    }
    return pageSize;
  }

}
