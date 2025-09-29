package com.jobportal.jobportal.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jobportal.jobportal.Entity.Job;
import com.jobportal.jobportal.Entity.User;
import com.jobportal.jobportal.Repository.JobRepository;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

	@Autowired
	private JobRepository jobRepository;

	// Create a new job
	public Job postJob(Job job) {
		return jobRepository.save(job);
	}

	// List all jobs
	public List<Job> getAllJobs() {
		return jobRepository.findAll();
	}

	// Get jobs posted by a specific employer
	public List<Job> getJobsByEmployer(User employer) {
		return jobRepository.findByEmployer(employer);
	}

	// Get job by ID
	public Optional<Job> getJobById(Long id) {
		return jobRepository.findById(id);
	}

	// Delete job
	public void deleteJob(Long id) {
		jobRepository.deleteById(id);
	}
	
}
