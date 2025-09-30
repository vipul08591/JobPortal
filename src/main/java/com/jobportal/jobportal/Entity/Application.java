package com.jobportal.jobportal.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private User candidate;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    private String resumeFileName; // uploaded resume file name
    private String status; // Pending, Viewed, Shortlisted, Rejected

    @Column(columnDefinition = "TEXT")
    private String coverLetter; // optional message from candidate

    private LocalDateTime appliedAt; // timestamp when applied
    

    // Constructors
    public Application() {
        this.appliedAt = LocalDateTime.now(); // default to now
        this.status = "Pending"; // default status
    }

    public Application(User candidate, Job job, String resumeFileName, String coverLetter) {
        this.candidate = candidate;
        this.job = job;
        this.resumeFileName = resumeFileName;
        this.coverLetter = coverLetter;
        this.appliedAt = LocalDateTime.now();
        this.status = "Pending";
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
 

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", candidate=" + candidate +
                ", job=" + job +
                ", status='" + status + '\'' +
                ", resumeFileName='" + resumeFileName + '\'' +
                ", coverLetter='" + coverLetter + '\'' +
                ", appliedAt=" + appliedAt +
                '}';
    }
}
