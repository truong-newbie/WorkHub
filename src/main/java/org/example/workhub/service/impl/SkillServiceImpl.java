package org.example.workhub.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.Specification.GenericSpecificationBuilder;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@RequiredArgsConstructor
@Log4j2
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
        log.info("DEBUG - SkillServiceImpl#getAll: keyword={}", request.getKeyword());

        String sortBy = Optional.ofNullable(request.getSortBy())
                .filter(s -> !s.trim().isEmpty())
                .orElse("id");

        Pageable pageable = PageRequest.of(
                request.getPageNum(),
                request.getPageSize(),
                request.getIsAscending()
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending()
        );


        GenericSpecificationBuilder<Skill> builder = new GenericSpecificationBuilder<>();

        String search = request.getKeyword();

        boolean added = false;
        if (search != null && !search.isBlank()) {
            Pattern pattern = Pattern.compile("(\\w+?)(>=|<=|!=|:|=|>|<)(\"[^\"]+\"|[^,|]+)(\\||,)");
            Matcher matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                String key = matcher.group(1);
                String operator = matcher.group(2);
                String value = matcher.group(3);
                String delimiter = matcher.group(4);

                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                boolean isOr = "|".equals(delimiter);
                String prefix = null;
                String suffix = null;

                if (value.startsWith("*")) {
                    prefix = "*";
                    value = value.substring(1);
                }
                if (value.endsWith("*")) {
                    suffix = "*";
                    value = value.substring(0, value.length() - 1);
                }
                builder.with(isOr, key, operator, value, prefix, suffix);
                added = true;
            }
        }
        // Nếu không khớp pattern, tự động tìm theo name LIKE %keyword%
        if (!added && search != null && !search.isBlank()) {
            builder.with("name", ":", search, "*", "*");
        }

        Specification<Skill> spec = builder.build();

        if (spec == null) {
            spec = Specification.where(SkillSpecification.isNotDeleted());
        } else {
            spec = spec.and(SkillSpecification.isNotDeleted());
        }

        Page<Skill> page = skillRepository.findAll(spec, pageable);

        List<SkillResponseDto> items = page.getContent()
                .stream()
                .map(skillMapper::toDto)
                .toList();

        PagingMeta meta = new PagingMeta(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber() + 1,
                page.getSize(),
                sortBy,
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
