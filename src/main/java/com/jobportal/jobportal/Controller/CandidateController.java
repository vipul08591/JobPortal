package com.jobportal.jobportal.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.jobportal.Entity.Application;
import com.jobportal.jobportal.Entity.CandidateProfile;
import com.jobportal.jobportal.Entity.Job;
import com.jobportal.jobportal.Entity.User;
import com.jobportal.jobportal.Repository.ApplicationRepository;
import com.jobportal.jobportal.Repository.JobRepository;
import com.jobportal.jobportal.Repository.UserRepository;
import com.jobportal.jobportal.Service.CandidateProfileService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/candidate")
public class CandidateController {

	@Autowired
	private JobRepository jobRepo;

	@Autowired
	private ApplicationRepository appRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private CandidateProfileService profileService;
	
	// ==========================
	// Dashboard
	// ==========================
	@GetMapping("/dashboard")
	public String dashboard(Model model, Authentication authentication, HttpSession session) {
	    if (authentication != null) {
	        String email = authentication.getName();
	        User user = userRepo.findByEmail(email);
	        if (user != null) {
	            model.addAttribute("username", user.getName());

	            // Only show the login message once
	            if (session.getAttribute("loginMessage") == null) {
	                model.addAttribute("loginMessage", "You have been logged in!");
	                session.setAttribute("loginMessage", "shown");
	            }

	            
	        }
	    }

	    // Fetch jobs
	    List<Job> jobs = jobRepo.findAll();
	    model.addAttribute("jobs", jobs);

	    return "candidate/dashboard";
	}


	// ==========================
	// Job Details
	// ==========================
	@GetMapping("/jobs/{id}")
	public String jobDetails(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
		Job job = jobRepo.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
		User candidate = userRepo.findByEmail(userDetails.getUsername());
		boolean alreadyApplied = appRepo.existsByJobAndCandidate(job, candidate);

		model.addAttribute("job", job);
		model.addAttribute("alreadyApplied", alreadyApplied);
		return "candidate/job-details";
	}

	// ==========================
	// Apply for Job
	// ==========================
	@PostMapping("/jobs/{id}/apply")
	public String applyForJob(@PathVariable Long id,
			@RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {

		User candidate = userRepo.findByEmail(userDetails.getUsername());
		Job job = jobRepo.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));

		if (appRepo.existsByJobAndCandidate(job, candidate)) {
			redirectAttributes.addFlashAttribute("errorMessage", "You have already applied for this job!");
			return "redirect:/candidate/dashboard"; // redirect to dashboard
		}

		CandidateProfile profile = profileService.getProfileByUser(candidate);
		String fileName = null;

		try {
			if (resumeFile != null && !resumeFile.isEmpty()) {
				fileName = System.currentTimeMillis() + "_" + resumeFile.getOriginalFilename();
				Path uploadPath = Paths.get("uploads/resumes").toAbsolutePath().normalize();
				if (!Files.exists(uploadPath))
					Files.createDirectories(uploadPath);
				Files.copy(resumeFile.getInputStream(), uploadPath.resolve(fileName));
			} else if (profile != null && profile.getProfileResumeFile() != null) {
				fileName = profile.getProfileResumeFile();
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Please upload a resume before applying.");
				return "redirect:/candidate/dashboard";
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload resume.");
			return "redirect:/candidate/dashboard";
		}

		Application application = new Application(candidate, job, fileName, null);
		appRepo.save(application);

		// Success flash message
		redirectAttributes.addFlashAttribute("successMessage",
				"✅ You have applied for '" + job.getTitle() + "' successfully!");
		return "redirect:/candidate/dashboard";
	}

	// ==========================
	// My Applications
	// ==========================
	@GetMapping("/applications")
	public String myApplications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User candidate = userRepo.findByEmail(userDetails.getUsername());
		List<Application> applications = appRepo.findByCandidate(candidate);
		model.addAttribute("applications", applications);
		return "candidate/my-applications";
	}

	// ==========================
	// Resume Download
	// ==========================
	@GetMapping("/resumes/{fileName:.+}")
	public ResponseEntity<Resource> getResume(@PathVariable String fileName) {
		try {
			// Correct path to resumes folder
			Path uploadPath = Paths.get("uploads/resumes").toAbsolutePath().normalize();
			Path file = uploadPath.resolve(fileName).normalize();
			Resource resource = new UrlResource(file.toUri());

			if (!resource.exists()) {
				return ResponseEntity.notFound().build();
			}

			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/pdf")
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"").body(resource);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	// ==========================
	// Candidate Profile Management
	// ==========================

	// View profile
	// View candidate profile
	@GetMapping("/profile")
	public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model,
			RedirectAttributes redirectAttributes) {
		try {
			// 1️⃣ Fetch logged-in User from DB
			User candidate = userRepo.findByEmail(userDetails.getUsername());
			if (candidate == null || candidate.getId() == null) {
				redirectAttributes.addFlashAttribute("errorMessage", "User not found in database");
				return "redirect:/login"; // redirect to login if user doesn't exist
			}

			// 2️⃣ Fetch profile; if none exists, create one linked to this user
			CandidateProfile profile = profileService.getProfileByUser(candidate);

			// 3️⃣ Calculate profile completion
			int completion = calculateProfileCompletion(profile);

			// 4️⃣ Add profile & completion to model
			model.addAttribute("profile", profile);
			model.addAttribute("profileCompletion", completion);

			// 5️⃣ Return Thymeleaf template
			return "candidate/profile";

		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to load profile. Please try again.");
			return "redirect:/candidate/dashboard"; // fallback page
		}
	}

	/**
	 * Calculate profile completion as a percentage. You can adjust weights based on
	 * importance of each section.
	 */
	private int calculateProfileCompletion(CandidateProfile profile) {
		int completion = 0;

		if (profile.getUser().getName() != null && !profile.getUser().getName().isEmpty())
			completion += 15;
		if (profile.getEducationList() != null && !profile.getEducationList().isEmpty())
			completion += 15;
		if (profile.getExperienceList() != null && !profile.getExperienceList().isEmpty())
			completion += 15;
		if (profile.getSkills() != null && !profile.getSkills().isEmpty())
			completion += 15;
		if (profile.getProfileResumeFile() != null && !profile.getProfileResumeFile().isEmpty())
			completion += 20;
		if (profile.getLinkedIn() != null || profile.getGithub() != null || profile.getPortfolioUrl() != null)
			completion += 20;

		return completion > 100 ? 100 : completion;
	}

	// Show Basic Info edit form
	@GetMapping("/profile/basic-info/edit")
	public String editBasicInfoForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);
		model.addAttribute("profile", profile);
		return "candidate/basic-info-edit"; // Thymeleaf template
	}

	// Handle Basic Info update
	@PostMapping("/profile/basic-info/update")
	public String updateBasicInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("name") String name,
			@RequestParam("phone") String phone, @RequestParam("location") String location,
			@RequestParam("profileSummary") String profileSummary,
			@RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
			RedirectAttributes redirectAttributes) {

		try {
			// 1️⃣ Fetch logged-in User from DB
			User user = userRepo.findByEmail(userDetails.getUsername());
			if (user == null) {
				redirectAttributes.addFlashAttribute("errorMessage", "User not found");
				return "redirect:/login";
			}

			// 2️⃣ Update editable User fields
			user.setName(name);
			userRepo.save(user); // Save updated name only

			// 3️⃣ Fetch or create CandidateProfile
			CandidateProfile profile = profileService.getProfileByUser(user);

			// 4️⃣ Handle profile picture upload
			if (profilePicture != null && !profilePicture.isEmpty()) {
				String fileName = System.currentTimeMillis() + "_" + profilePicture.getOriginalFilename();
				Path uploadPath = Paths.get("uploads/profiles").toAbsolutePath().normalize();
				if (!Files.exists(uploadPath))
					Files.createDirectories(uploadPath);
				Files.copy(profilePicture.getInputStream(), uploadPath.resolve(fileName));
				profile.setProfilePicture(fileName);
			}

			// 5️⃣ Update other profile fields
			profile.setPhone(phone);
			profile.setLocation(location);
			profile.setProfileSummary(profileSummary);

			profileService.saveProfile(profile);

			redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile.");
		}

		return "redirect:/candidate/profile";
	}

	/// Show Resume Edit Page
	@GetMapping("/profile/resume/edit")
	public String showResumeEdit(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// Fetch User first
		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);
		model.addAttribute("profile", profile);
		return "candidate/resume-edit"; // Thymeleaf template
	}

	// Handle Resume Upload / Replace
	@PostMapping("/profile/resume/update")
	public String updateResume(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("resumeFile") MultipartFile resumeFile, RedirectAttributes redirectAttributes) {

		// 1️⃣ Fetch User and Profile
		User candidate = userRepo.findByEmail(userDetails.getUsername());
		if (candidate == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
			return "redirect:/candidate/profile/resume/edit";
		}

		CandidateProfile profile = profileService.getProfileByUser(candidate);

		// 2️⃣ Validate file
		if (resumeFile == null || resumeFile.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload.");
			return "redirect:/candidate/profile/resume/edit";
		}

		String filename = resumeFile.getOriginalFilename().toLowerCase();
		if (!(filename.endsWith(".pdf") || filename.endsWith(".doc") || filename.endsWith(".docx"))) {
			redirectAttributes.addFlashAttribute("errorMessage", "Invalid file type. Only PDF, DOC, DOCX allowed.");
			return "redirect:/candidate/profile/resume/edit";
		}

		if (resumeFile.getSize() > 5 * 1024 * 1024) { // 5MB
			redirectAttributes.addFlashAttribute("errorMessage", "File too large. Maximum size allowed is 5MB.");
			return "redirect:/candidate/profile/resume/edit";
		}

		try {
			// 3️⃣ Delete old resume if exists
			String uploadDir = "uploads/resumes/";
			if (profile.getProfileResumeFile() != null) {
				Path oldPath = Paths.get(uploadDir + profile.getProfileResumeFile());
				if (Files.exists(oldPath))
					Files.delete(oldPath);
			}

			// 4️⃣ Save new resume file
			String newFilename = candidate.getId() + "_" + System.currentTimeMillis() + "_"
					+ resumeFile.getOriginalFilename();
			Path path = Paths.get(uploadDir + newFilename);
			if (!Files.exists(path.getParent()))
				Files.createDirectories(path.getParent());
			Files.write(path, resumeFile.getBytes());

			// 5️⃣ Update profile and save
			profile.setProfileResumeFile(newFilename);
			profileService.saveProfile(profile);

			// 6️⃣ Set success message
			redirectAttributes.addFlashAttribute("successMessage", "Resume updated successfully!");
			return "redirect:/candidate/profile";

		} catch (IOException e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload resume. Please try again.");
			return "redirect:/candidate/profile/resume/edit";
		}
	}

	// Show Skills Edit Page
	@GetMapping("/profile/skills/edit")
	public String showSkillsEdit(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);
		model.addAttribute("profile", profile);
		return "candidate/skills-edit";
	}

	// Update Skills
	@PostMapping("/profile/skills/update")
	public String updateSkills(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(value = "skills", required = false) List<String> skills,
			RedirectAttributes redirectAttributes) {

		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);

		if (skills == null || skills.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Please add at least one skill before saving.");
			return "redirect:/candidate/profile/skills/edit"; // back to edit page
		}

		// Remove empty strings or null values from the list
		skills.removeIf(s -> s == null || s.trim().isEmpty());

		profile.setSkills(skills != null ? new HashSet<>(skills) : Set.of());

		profileService.saveProfile(profile);

		redirectAttributes.addFlashAttribute("successMessage", "Skills updated successfully!");
		return "redirect:/candidate/profile";
	}

	// Show Experience Edit Page
	@GetMapping("/profile/experience/edit")
	public String showExperienceEdit(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);

		// Ensure the experience list is never null
		if (profile.getExperienceList() == null) {
			profile.setExperienceList(new ArrayList<>());
		}

		model.addAttribute("profile", profile);
		return "candidate/experience-edit";
	}

	// Update Experience
	@PostMapping("/profile/experience/update")
	public String updateExperience(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(value = "role", required = false) List<String> roles,
			@RequestParam(value = "company", required = false) List<String> companies,
			@RequestParam(value = "startDate", required = false) List<String> startDates,
			@RequestParam(value = "endDate", required = false) List<String> endDates,
			@RequestParam(value = "description", required = false) List<String> descriptions,
			RedirectAttributes redirectAttributes) {

		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);

		// Clear previous experiences
		profile.getExperienceList().clear();

		if (roles != null && !roles.isEmpty()) {
			for (int i = 0; i < roles.size(); i++) {
				CandidateProfile.Experience exp = new CandidateProfile.Experience();
				exp.setRole(roles.get(i));
				exp.setCompany(companies != null && companies.size() > i ? companies.get(i) : "");
				exp.setStartDate(startDates != null && startDates.size() > i ? startDates.get(i) : "");

				// Safely handle end date; default to "Present" if not provided
				if (endDates != null && endDates.size() > i && endDates.get(i) != null && !endDates.get(i).isEmpty()) {
					exp.setEndDate(endDates.get(i));
				} else {
					exp.setEndDate("Present");
				}

				exp.setDescription(descriptions != null && descriptions.size() > i ? descriptions.get(i) : "");
				profile.getExperienceList().add(exp);
			}
		}

		profileService.saveProfile(profile);
		redirectAttributes.addFlashAttribute("successMessage", "Experience updated successfully!");
		return "redirect:/candidate/profile";
	}

	// Show Education Edit Page
	@GetMapping("/profile/education/edit")
	public String showEducationEdit(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);
		model.addAttribute("profile", profile);
		return "candidate/education-edit"; // Thymeleaf template
	}

	@PostMapping("/profile/education/update")
	public String updateEducation(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(value = "degree", required = false) List<String> degrees,
			@RequestParam(value = "institution", required = false) List<String> institutions,
			@RequestParam(value = "startYear", required = false) List<String> startYears,
			@RequestParam(value = "endYear", required = false) List<String> endYears,
			RedirectAttributes redirectAttributes) {

		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);

		profile.getEducationList().clear();

		if (degrees != null && !degrees.isEmpty()) {
			for (int i = 0; i < degrees.size(); i++) {
				CandidateProfile.Education edu = new CandidateProfile.Education();
				edu.setDegree(degrees.get(i));
				edu.setInstitution(institutions.get(i));
				edu.setStartYear(startYears.get(i));
				edu.setEndYear(endYears.get(i));
				profile.getEducationList().add(edu);
			}
		}

		profileService.saveProfile(profile);
		redirectAttributes.addFlashAttribute("successMessage", "Education updated successfully!");
		return "redirect:/candidate/profile";
	}

	// Show Languages Edit Page
	@GetMapping("/profile/languages/edit")
	public String showLanguagesEdit(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);
		model.addAttribute("profile", profile);
		return "candidate/language-edit"; // Thymeleaf template
	}

	// Handle Languages Update
	@PostMapping("/profile/languages/update")
	public String updateLanguages(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(value = "languages", required = false) List<String> languages,
			RedirectAttributes redirectAttributes) {

		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);

		// If no languages selected, clear the list
		profile.setLanguages(languages != null ? new HashSet<>(languages) : Set.of());

		profileService.saveProfile(profile);

		redirectAttributes.addFlashAttribute("successMessage", "Languages updated successfully!");
		return "redirect:/candidate/profile";
	}

	@GetMapping("/profile/social/edit")
	public String showSocialEdit(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);
		model.addAttribute("profile", profile);
		return "candidate/social-edit"; // Thymeleaf template
	}

	// ==========================
	// Handle Social / Portfolio Update
	// ==========================
	@PostMapping("/profile/social/update")
	public String updateSocial(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(value = "linkedIn", required = false) String linkedIn,
			@RequestParam(value = "github", required = false) String github,
			@RequestParam(value = "portfolioUrl", required = false) String portfolioUrl,
			RedirectAttributes redirectAttributes) {

		User candidate = userRepo.findByEmail(userDetails.getUsername());
		CandidateProfile profile = profileService.getProfileByUser(candidate);

		try {
			profile.setLinkedIn(linkedIn);
			profile.setGithub(github);
			profile.setPortfolioUrl(portfolioUrl);

			profileService.saveProfile(profile);

			redirectAttributes.addFlashAttribute("successMessage", "Social links updated successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to update social links. Please try again.");
		}

		return "redirect:/candidate/profile";

	}

}
