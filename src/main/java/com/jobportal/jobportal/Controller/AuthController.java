package com.jobportal.jobportal.Controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.jobportal.Entity.User;
import com.jobportal.jobportal.Repository.UserRepository;

import jakarta.validation.Valid;

@Controller
public class AuthController {

	private final UserRepository userRepo;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public AuthController(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	@GetMapping("/")
	public String home() {
		return "home"; // returns home.html
	}

	@GetMapping("/register")
	public String showRegisterForm(User user) {
		return "register";
	}

	@PostMapping("/register")
	public String processRegister(@Valid @ModelAttribute("user") User user,
	                              BindingResult result,
	                              RedirectAttributes redirectAttributes) {

	    // Check if email already exists
	    if (userRepo.findByEmail(user.getEmail()) != null) {
	        result.rejectValue("email", "error.user", "Email is already registered");
	    }

	    if (result.hasErrors()) {
	        return "register"; // redisplay form with errors
	    }

	    // Encode password and save
	    user.setPassword(passwordEncoder.encode(user.getPassword()));
	    userRepo.save(user);

	    // Add flash attribute for success message
	    redirectAttributes.addFlashAttribute("successMessage", "Registered successfully!");

	    // Redirect to /register so the message shows as a flash
	    return "redirect:/register";
	}

}
