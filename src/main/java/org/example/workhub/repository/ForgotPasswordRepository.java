package org.example.workhub.repository;

import org.example.workhub.domain.entity.ForgotPassword;
import org.example.workhub.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, User user);

    @Modifying
    @Query("DELETE FROM ForgotPassword fp WHERE fp.user = :user")
    void deleteByUser(@Param("user") User user);
}
