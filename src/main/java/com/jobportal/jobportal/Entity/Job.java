package com.jobportal.jobportal.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;              // Job title

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;        // Job description

    private String location;           // Job location (e.g., remote or specific city)

    private String type;               // Full-time, Part-time, Internship

    private String companyName;        // Name of company

    private String companyLocation;    // Company headquarters or main location

    private String salary;             // Salary info, e.g., "₹50k-70k/month"

    private LocalDateTime postedAt;    // For "New" badge

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private User employer;

    // --- Constructors ---
    public Job() {
        this.postedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLocation() { return companyLocation; }
    public void setCompanyLocation(String companyLocation) { this.companyLocation = companyLocation; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }

    public User getEmployer() { return employer; }
    public void setEmployer(User employer) { this.employer = employer; }

    // --- Helper method for "New" badge ---
    @Transient
    public boolean isNew() {
        return postedAt != null && postedAt.isAfter(LocalDateTime.now().minusDays(3));
    }
}
