package org.example.workhub.repository;

import org.example.workhub.domain.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    @Query("SELECT j FROM Job j WHERE j.id = :id AND j.deleted = false")
    Optional<Job> findByIdNotDeleted(@Param("id") Long id);

    @Query("SELECT j FROM Job j WHERE j.company.id = :companyId AND j.deleted = false")
    List<Job> findByCompanyIdNotDeleted(@Param("companyId") Long companyId);

    @Query("SELECT j FROM Job j WHERE j.recruiter.id = :recruiterId AND j.deleted = false")
    List<Job> findByRecruiterIdNotDeleted(@Param("recruiterId") String recruiterId);

    @Query("SELECT j FROM Job j WHERE j.published = true AND j.deleted = false")
    List<Job> findAllPublished();

    boolean existsBySlug(String slug);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.deleted = false")
    long countNotDeleted();

    @Query("SELECT COUNT(j) FROM Job j WHERE j.published = true AND j.deleted = false")
    long countPublished();

    @Query("SELECT COUNT(j) FROM Job j WHERE j.published = false AND j.deleted = false")
    long countDraft();
}