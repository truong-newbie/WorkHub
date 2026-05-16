package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.response.CompanyResponseDto;
import org.example.workhub.domain.entity.Company;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    default CompanyResponseDto toDto(Company company) {
        if (company == null) {
            return null;
        }
        return CompanyResponseDto.builder()
                .id(company.getId())
                .name(company.getName())
                .slug(company.getSlug())
                .description(company.getDescription())
                .website(company.getWebsite())
                .email(company.getEmail())
                .phone(company.getPhone())
                .address(company.getAddress())
                .city(company.getCity())
                .country(company.getCountry())
                .companySize(company.getCompanySize())
                .industry(company.getIndustry())
                .taxCode(company.getTaxCode())
                .active(company.getActive())
                .verified(company.getVerified())
                .deleted(company.getDeleted())
                .logo(company.getLogo())
                .coverImage(company.getCoverImage())
                .owner(mapOwner(company))
                .build();
    }

    default List<CompanyResponseDto> toDtoList(List<Company> companies) {
        if (companies == null) {
            return List.of();
        }
        return companies.stream().map(this::toDto).collect(Collectors.toList());
    }

    private CompanyResponseDto.OwnerSummary mapOwner(Company company) {
        if (company.getOwner() == null) {
            return null;
        }
        return CompanyResponseDto.OwnerSummary.builder()
                .id(company.getOwner().getId())
                .username(company.getOwner().getUsername())
                .email(company.getOwner().getEmail())
                .build();
    }
}
