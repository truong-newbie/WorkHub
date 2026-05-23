package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.response.CompanyResponseDto;
import org.example.workhub.domain.entity.Company;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponseDto toDto(Company company);

    List<CompanyResponseDto> toDtoList(List<Company> companies);

}
