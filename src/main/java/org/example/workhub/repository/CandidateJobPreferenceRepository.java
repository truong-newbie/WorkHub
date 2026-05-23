package org.example.workhub.repository;

import org.example.workhub.domain.entity.CandidateJobPreference;
import org.example.workhub.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateJobPreferenceRepository extends JpaRepository<CandidateJobPreference, Long> {

    Optional<CandidateJobPreference> findByCandidateId(String candidateId);

    boolean existsByCandidateId(String candidateId);

    Optional<CandidateJobPreference> findByCandidate(User candidate);
}
