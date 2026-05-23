package org.example.workhub.repository;

import org.example.workhub.constant.StatusEnum;
import org.example.workhub.domain.entity.CompanyJoinRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyJoinRequestRepository extends JpaRepository<CompanyJoinRequest, Long> {

    boolean existsByRecruiterIdAndCompanyIdAndStatus(String recruiterId, Long companyId, StatusEnum status);

    Page<CompanyJoinRequest> findByRecruiterIdOrderByCreatedDateDesc(String recruiterId, Pageable pageable);

    Page<CompanyJoinRequest> findByCompanyIdOrderByCreatedDateDesc(Long companyId, Pageable pageable);

    List<CompanyJoinRequest> findByRecruiterIdAndStatusAndIdNot(String recruiterId, StatusEnum status, Long id);
}
