package org.example.workhub.repository;

import org.example.workhub.domain.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    UserSession findByToken(String token);

    List<UserSession> findAllByEmail(String email);

    UserSession findByIpAddressAndEmail(String ipAddress, String email);
}
