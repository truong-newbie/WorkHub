package org.example.workhub.domain.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.LevelEnum;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Skill;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class JobDto {
    private Long id;
    private String name;

    private String location;

    private Double salary;

    private Integer quantity;

    private LevelEnum level;

    private String description;


    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean active;

    private List<String> skillNames;

    private String companyName;
}
