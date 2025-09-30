package com.jobportal.jobportal.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jobportal.jobportal.Entity.Application;
import com.jobportal.jobportal.Entity.Job;
import com.jobportal.jobportal.Entity.User;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

	List<Application> findByJob(Job job);

	List<Application> findByCandidate(User candidate);

	boolean existsByJobAndCandidate(Job job, User candidate);

	Optional<Application> findByResumeFileName(String resumeFileName);
	


}
