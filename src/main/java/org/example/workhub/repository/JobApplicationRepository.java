package org.example.workhub.repository;

import org.example.workhub.constant.StatusEnum;
import org.example.workhub.domain.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {

    @Query("SELECT ja FROM JobApplication ja WHERE ja.id = :id")
    Optional<JobApplication> findById(@Param("id") Long id);

    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.id = :jobId AND ja.deleted = false")
    Page<JobApplication> findByJobId(@Param("jobId") Long jobId, Pageable pageable);

    @Query("SELECT ja FROM JobApplication ja WHERE ja.user.id = :userId AND ja.deleted = false")
    Page<JobApplication> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.id = :jobId AND ja.user.id = :userId AND ja.deleted = false")
    Optional<JobApplication> findByJobIdAndUserId(@Param("jobId") Long jobId, @Param("userId") String userId);

    boolean existsByJobIdAndUserIdAndDeletedFalse(Long jobId, String userId);

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.job.id = :jobId AND ja.deleted = false")
    long countByJobId(@Param("jobId") Long jobId);

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.status = :status AND ja.deleted = false")
    long countByStatus(@Param("status") StatusEnum status);

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.deleted = false")
    long countTotal();

    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.company.id = :companyId AND ja.deleted = false")
    Page<JobApplication> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}