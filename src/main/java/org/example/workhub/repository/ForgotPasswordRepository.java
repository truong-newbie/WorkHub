package org.example.workhub.repository;

import org.example.workhub.domain.entity.ForgotPassword;
import org.example.workhub.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, User user);
}
