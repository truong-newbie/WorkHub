package org.example.workhub.domain.dto.request.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.LevelEnum;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Resume;
import org.example.workhub.domain.entity.Skill;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class JobCreateDto {

    @NotBlank(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    private String name;

    @NotBlank(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    private String location;

    @NotNull(message = ErrorMessage.NOT_NULL_FIELD)
    private Double salary;

    @NotNull(message = ErrorMessage.NOT_NULL_FIELD)
    private Integer quantity;

    @NotNull(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    private LevelEnum level;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String description;

    @NotNull(message = ErrorMessage.NOT_NULL_FIELD)
    private LocalDateTime startDate;

    @NotNull(message = ErrorMessage.NOT_NULL_FIELD)
    private LocalDateTime endDate;

    private Boolean active;

    private List<Long> skillIds;


    private Company company;

}