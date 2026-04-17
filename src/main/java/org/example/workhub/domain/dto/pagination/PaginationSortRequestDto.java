package org.example.workhub.domain.dto.pagination;


import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.CommonConstant;
import org.example.workhub.constant.SortByDataConstant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaginationSortRequestDto extends PaginationRequestDto {

  @Parameter(description = "The name of property want to sort")
  private String sortBy = CommonConstant.EMPTY_STRING;

  @Parameter(description = "Sorting criteria - Default sort order is descending")
  private Boolean isAscending = Boolean.FALSE;

  public PaginationSortRequestDto(Integer pageNum, Integer pageSize, String sortBy, boolean isAscending) {
    super(pageNum, pageSize);
    this.sortBy = sortBy;
    this.isAscending = isAscending;
  }

  public String getSortBy(SortByDataConstant constant) {
    return constant.getSortBy(sortBy);
  }

}
