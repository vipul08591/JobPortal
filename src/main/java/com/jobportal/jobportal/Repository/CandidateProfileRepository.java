package com.jobportal.jobportal.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.jobportal.jobportal.Entity.CandidateProfile;
import com.jobportal.jobportal.Entity.User;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {
    Optional<CandidateProfile> findByUserId(Long userId);    // existing
    Optional<CandidateProfile> findByUser(User user);        // added for convenience
}
