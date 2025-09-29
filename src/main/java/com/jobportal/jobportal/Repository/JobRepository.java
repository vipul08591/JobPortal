package com.jobportal.jobportal.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jobportal.jobportal.Entity.Job;
import com.jobportal.jobportal.Entity.User;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
	List<Job> findByEmployer(User employer);
	
	
	
	 
}
