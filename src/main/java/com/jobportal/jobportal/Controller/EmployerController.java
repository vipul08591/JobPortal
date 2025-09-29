package com.jobportal.jobportal.Controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.jobportal.Entity.Application;
import com.jobportal.jobportal.Entity.CandidateProfile;
import com.jobportal.jobportal.Entity.Job;
import com.jobportal.jobportal.Entity.User;
import com.jobportal.jobportal.Repository.ApplicationRepository;
import com.jobportal.jobportal.Repository.JobRepository;
import com.jobportal.jobportal.Repository.UserRepository;
import com.jobportal.jobportal.Service.CandidateProfileService;

@Controller
@RequestMapping("/employer")
public class EmployerController {

	@Autowired
	private JobRepository jobRepo;

	@Autowired
	private ApplicationRepository applicationRepo;

	@Autowired
	private UserRepository userRepo;
	

	@Autowired
	private CandidateProfileService profileService;

	// Employer dashboard
	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User employer = userRepo.findByEmail(userDetails.getUsername());
		List<Job> jobs = jobRepo.findByEmployer(employer); // Only jobs posted by this employer
		model.addAttribute("jobs", jobs);
		return "employer/dashboard";
	}

	// Show form to post a new job
	@GetMapping("/jobs/new")
	public String showJobForm(Model model) {
		model.addAttribute("job", new Job());
		return "employer/job-form";
	}

	// Save new job
	@PostMapping("/jobs")
	public String saveJob(@ModelAttribute Job job, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User employer = userRepo.findByEmail(userDetails.getUsername());
		job.setEmployer(employer);
		jobRepo.save(job);

		redirectAttributes.addFlashAttribute("successMessage", "✅ Job posted successfully!");
		return "redirect:/employer/dashboard";
	}

	// View job details
	@GetMapping("/jobs/{id}")
	public String jobDetails(@PathVariable Long id, Model model) {
		Job job = jobRepo.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
		model.addAttribute("job", job);
		return "employer/job-details";
	}

	// View candidates who applied for a job
	@GetMapping("/jobs/{id}/applications")
	public String viewApplications(@PathVariable Long id, Model model) {
	    Job job = jobRepo.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
	    List<Application> applications = applicationRepo.findByJob(job);

	    // Automatically mark as Viewed
	    for (Application app : applications) {
	        if ("Pending".equals(app.getStatus())) {
	            app.setStatus("Viewed");
	            applicationRepo.save(app);
	        }
	    }

	    model.addAttribute("applications", applications);
	    model.addAttribute("job", job);
	    return "employer/applications";
	}


	// Delete job
	@PostMapping("/jobs/{id}/delete")
	public String deleteJob(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		Job job = jobRepo.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
		applicationRepo.findByJob(job).forEach(applicationRepo::delete);
		jobRepo.delete(job);

		redirectAttributes.addFlashAttribute("deletedMessage", "✅ Job deleted successfully!");
		return "redirect:/employer/dashboard";
	}

	// ✅ Download / view candidate resume
	@GetMapping("/resumes/{fileName}")
	@ResponseBody
	public ResponseEntity<Resource> getResume(@PathVariable String fileName) {
	    try {
	        // Fetch the application associated with this resume
	        Optional<Application> optionalApp = applicationRepo.findByResumeFileName(fileName);

	        if (optionalApp.isPresent()) {
	            Application app = optionalApp.get();
	            if (!"Viewed".equals(app.getStatus())) {
	                app.setStatus("Viewed");
	                applicationRepo.save(app);
	            }
	        }

	        Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
	        Path file = uploadPath.resolve(fileName).normalize();
	        Resource resource = new UrlResource(file.toUri());

	        if (!resource.exists()) {
	            return ResponseEntity.notFound().build();
	        }

	        String contentType = "application/pdf";
	        String displayName = fileName.contains("_") ? fileName.substring(fileName.indexOf("_") + 1) : fileName;

	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_TYPE, contentType)
	                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + displayName + "\"")
	                .body(resource);

	    } catch (Exception e) {
	        return ResponseEntity.badRequest().build();
	    }
	}

	
	@PostMapping("/applications/{id}/status")
	@ResponseBody
	public String updateApplicationStatus(@PathVariable Long id,
	                                      @RequestParam("status") String status) {
	    Application app = applicationRepo.findById(id)
	            .orElseThrow(() -> new RuntimeException("Application not found"));
	    app.setStatus(status);
	    applicationRepo.save(app);
	    return "success";
	}

	 // View candidate profile
    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAuthority('EMPLOYER')") // only employer role can access
    public String viewCandidateProfile(@PathVariable Long candidateId, Model model) {
        User candidate = userRepo.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        CandidateProfile profile = profileService.getProfileByUser(candidate);
        model.addAttribute("profile", profile);
        return "employer/candidate-profile"; // Thymeleaf page
    }
}