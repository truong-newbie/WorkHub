package org.example.workhub.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.workhub.domain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByIdAndDeletedFalse(Long id);

    Page<Company> findByNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

    boolean existsByName(@NotBlank(message = "Ten cong ty khong duoc de trong") String name);
}
