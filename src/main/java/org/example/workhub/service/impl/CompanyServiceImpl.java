package org.example.workhub.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.SortByDataConstant;
import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.CompanyRequestDto;
import org.example.workhub.domain.dto.response.CompanyResponseDto;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.mapper.CompanyMapper;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.CompanyRepository;
import org.example.workhub.service.CompanyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    CompanyRepository companyRepository;
    CompanyMapper companyMapper;

    @Override
    public PaginationResponseDto<CompanyResponseDto> getAll(PaginationFullRequestDto request) {
        Pageable pageable = PageRequest.of(
                request.getPageNum(),
                request.getPageSize(),
                request.getIsAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSortBy(SortByDataConstant.COMPANY)
        );

        Page<Company> page;

        if (!request.getKeyword().isEmpty()) {
            page = companyRepository.findByNameContainingIgnoreCaseAndDeletedFalse(request.getKeyword(), pageable);
        } else {
            page = companyRepository.findAll(pageable);
        }

        List<CompanyResponseDto> items = new ArrayList<>();

        for (Company company : page.getContent()) {
            CompanyResponseDto dto = companyMapper.toDto(company);
            items.add(dto);
        }

        PagingMeta meta = new PagingMeta(
                page.getTotalElements(),
                page.getTotalPages(),
                request.getPageNum() + 1,
                request.getPageSize(),
                request.getSortBy(SortByDataConstant.COMPANY),
                request.getIsAscending() ? "ASC" : "DESC"
        );

        return new PaginationResponseDto<>(meta, items);
    }

    @Override
    public CompanyResponseDto getById(Long id) {

        Company company = companyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.Company.ERR_NOT_FOUND));
        return companyMapper.toDto(company);
    }

    @Override
    public CompanyResponseDto create(CompanyRequestDto request) {
        Company company= new Company();

        if(companyRepository.existsByName(request.getName())){
            throw new BadRequestException(ErrorMessage.Company.ERR_ALREADY_EXISTS_COMPANY);
        }
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setAddress(request.getAddress());
        company.setLogo(request.getLogo());
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    public CompanyResponseDto update(Long id, CompanyRequestDto request) {
        Company company = companyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.Company.ERR_NOT_FOUND));

        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setAddress(request.getAddress());
        company.setLogo(request.getLogo());
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    public void delete(Long id) {
        Company company = companyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.Company.ERR_NOT_FOUND));

        companyRepository.delete(company);
    }
}
