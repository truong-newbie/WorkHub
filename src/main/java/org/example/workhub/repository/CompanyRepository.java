package org.example.workhub.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.workhub.domain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    Optional<Company> findByIdAndDeletedFalse(Long id);

    Page<Company> findByNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

    Optional<Company> findByOwnerIdAndDeletedFalse(String ownerId);

    Optional<Company> findBySlugAndDeletedFalse(String slug);

    @Query("""
            SELECT c FROM Company c
            JOIN c.users u
            WHERE u.id = :userId AND c.deleted = false
            """)
    Optional<Company> findByUserIdAndDeletedFalse(@Param("userId") String userId);

    boolean existsByNameIgnoreCaseAndDeletedFalse(@NotBlank String name);

    boolean existsByNameIgnoreCaseAndDeletedFalseAndIdNot(@NotBlank String name, Long id);

    boolean existsBySlugAndDeletedFalse(String slug);

    boolean existsBySlugAndDeletedFalseAndIdNot(String slug, Long id);
}
