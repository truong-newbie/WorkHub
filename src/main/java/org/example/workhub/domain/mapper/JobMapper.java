package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.request.job.JobCreateDto;
import org.example.workhub.domain.dto.request.job.JobUpdateDto;
import org.example.workhub.domain.dto.response.JobDto;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobMapper {
    Job toJob(JobCreateDto jobCreateDto);




    @Mapping(target = "companyName", source = "job.company.name")
    @Mapping(target = "skillNames", source = "job.skills")
    JobDto toJobDto(Job job);

    default List<String> mapSkillsToSkillNames(List<Skill> skills){
        if(skills == null){
            return null;
        }
        return skills.stream()
                .map(Skill::getName)
                .toList();
    }

    List<JobDto> toJobDtoList(List<Job> jobs);

}
