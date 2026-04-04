package org.example.workhub.service.impl;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.SkillRequestDto;
import org.example.workhub.domain.dto.response.SkillResponseDto;
import org.example.workhub.domain.dto.specification.SkillSpecification;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.mapper.SkillMapper;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.SkillRepository;
import org.example.workhub.service.SkillService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@RequiredArgsConstructor
public class SkillServiceImpl  implements SkillService {

    SkillRepository skillRepository;
    SkillMapper skillMapper;

    @Override
    public SkillResponseDto create( SkillRequestDto request) {
        if(skillRepository.existsByNameAndDeletedFalse(request.getName().trim())){
            throw new ConflictException(ErrorMessage.Skill.ERR_ALREADY_EXISTS_SKILL);
        }
        Skill skill = skillMapper.toEntity(request);
        return skillMapper.toDto(skillRepository.save(skill));
    }

    @Override
    public SkillResponseDto update(Long id, SkillRequestDto request) {
        Skill skill= skillRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.Skill.ERR_NOT_FOUND));

        if(skillRepository.existsByNameAndDeletedFalse(request.getName())
        && !skill.getName().equals(request.getName().trim())){
            throw new ConflictException(ErrorMessage.Skill.ERR_ALREADY_EXISTS_SKILL);
        }

        skill.setName(request.getName());
        return skillMapper.toDto(skillRepository.save(skill));

    }

    @Override
    public SkillResponseDto getById(Long id) {
        Skill skill= skillRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.Skill.ERR_NOT_FOUND));
        return skillMapper.toDto(skill);
    }

    @Override
    public PaginationResponseDto<SkillResponseDto> getAll(PaginationFullRequestDto request) {
        String sortBy = Optional.ofNullable(request.getSortBy())
                .filter(s -> !s.trim().isEmpty())
                .orElse("id");

        Pageable pageable = (Pageable) PageRequest.of(
                request.getPageNum(),
                request.getPageSize(),
                request.getIsAscending()
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending()
        );

        Page<Skill> page = skillRepository.findAll(
                Specification.where(SkillSpecification.search(request.getKeyword()))
                        .and(SkillSpecification.isNotDeleted()),
                pageable
        );

        List<SkillResponseDto> items = page.getContent()
                .stream()
                .map(skillMapper::toDto)
                .toList();

        PagingMeta meta = new PagingMeta(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber() + 1,
                page.getSize(),
                request.getSortBy(),
                request.getIsAscending() ? "ASC" : "DESC"
        );

        return new PaginationResponseDto<>(meta, items);

    }

    @Override
    public void delete(Long id) {
        Skill skill= skillRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.Skill.ERR_NOT_FOUND));

        skillRepository.delete(skill);
    }
}
