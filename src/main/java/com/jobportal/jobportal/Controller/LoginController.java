package com.jobportal.jobportal.Controller;

import com.jobportal.jobportal.Entity.User;
import com.jobportal.jobportal.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final UserRepository userRepo;

    public LoginController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/default")
    public String defaultAfterLogin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepo.findByEmail(email);

        if (user.getRole().equals("CANDIDATE")) return "redirect:/candidate/dashboard";
        else if (user.getRole().equals("EMPLOYER")) return "redirect:/employer/dashboard";
        else return "redirect:/";
    }
}
