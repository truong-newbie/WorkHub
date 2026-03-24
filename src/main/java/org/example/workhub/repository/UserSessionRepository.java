package org.example.workhub.repository;

import org.example.workhub.domain.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    UserSession findByToken(String token);

    List<UserSession> findAllByEmail(String email);

    UserSession findByIpAddressAndEmail(String ipAddress, String email);
}
