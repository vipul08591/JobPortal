package com.jobportal.jobportal.Service;


import com.jobportal.jobportal.Entity.Application;
import com.jobportal.jobportal.Entity.Job;
import com.jobportal.jobportal.Entity.User;
import com.jobportal.jobportal.Repository.ApplicationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    // Candidate applies for a job
    public Application applyForJob(Application application) {
        return applicationRepository.save(application);
    }

    // List all applications for a candidate
    public List<Application> getApplicationsByCandidate(User candidate) {
        return applicationRepository.findByCandidate(candidate);
    }

    // List all applications for a job (for employer)
    public List<Application> getApplicationsByJob(Job job) {
        return applicationRepository.findByJob(job);
    }

    // Get application by ID
    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }
}
