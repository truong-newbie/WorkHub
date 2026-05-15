package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.response.FavoriteJobResponse;
import org.example.workhub.domain.dto.response.JobResponse;
import org.example.workhub.domain.entity.FavoriteJob;
import org.example.workhub.domain.entity.Job;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class FavoriteJobMapper {

    @Autowired
    protected JobMapper jobMapper;

    protected FavoriteJobMapper() {
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    public abstract void updateFromRequest(Object request, @MappingTarget FavoriteJob favoriteJob);

    @Mapping(target = "savedAt", source = "createdAt")
    @Mapping(target = "job", expression = "java(mapJobToResponse(favoriteJob.getJob()))")
    public abstract FavoriteJobResponse toResponse(FavoriteJob favoriteJob);

    public abstract List<FavoriteJobResponse> toResponses(List<FavoriteJob> favoriteJobs);

    protected JobResponse mapJobToResponse(Job job) {
        if (job == null) return null;
        return jobMapper.toResponse(job);
    }
}