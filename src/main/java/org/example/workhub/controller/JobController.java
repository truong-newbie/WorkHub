package org.example.workhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.RestData;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.pagination.PaginationSortRequestDto;
import org.example.workhub.domain.dto.request.UserCreateDto;
import org.example.workhub.domain.dto.request.UserUpdateDto;
import org.example.workhub.domain.dto.request.job.JobCreateDto;
import org.example.workhub.domain.dto.request.job.JobUpdateDto;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.example.workhub.base.VsResponseUtil.error;
import static org.example.workhub.base.VsResponseUtil.success;

@RestApiV1
@RequiredArgsConstructor
@Valid
public class JobController {
    private final JobService jobService;


    @GetMapping(UrlConstant.Job.GET_JOB)
    public ResponseEntity<RestData<?>> getUser(@PathVariable Long id){
        try{
            return success(HttpStatus.OK,jobService.getJob(id) );
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }



    @PostMapping(UrlConstant.Job.CREATE_JOB)
    public ResponseEntity<RestData<?>> createJob(@RequestBody @Valid JobCreateDto job){
        try{
            return success(HttpStatus.CREATED,jobService.createJob(job) );
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping(UrlConstant.Job.UPDATE_JOB)
    public ResponseEntity<RestData<?>> updateUser(@RequestBody @Valid JobUpdateDto jobUpdateDto){
        try{
            return success(HttpStatus.OK, jobService.updateJob(jobUpdateDto) );
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping(UrlConstant.Job.DELETE_JOB)
    public ResponseEntity<RestData<?>> deleteUser(@PathVariable Long id){
        try{
            jobService.deleteJob(id);
            return success(HttpStatus.OK, null);
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @GetMapping(UrlConstant.Job.GET_JOBS)
    public ResponseEntity<RestData<?>> getListUser(
            @RequestParam(value = "filter", required = false) List<String> filter,
            PaginationSortRequestDto pageable
    ){
        try{
            return success(HttpStatus.OK,jobService.getAllJobs(filter,pageable) );
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
