package com.jobportal.jobportal.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jobportal.jobportal.Entity.User;
import com.jobportal.jobportal.Repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// Save a new user
	public User registerUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}

	// Check if email exists
	public boolean emailExists(String email) {
		return userRepository.findByEmail(email) != null;
	}

	// Find user by email (for login)
	public Optional<User> findByEmail(String email) {
		return Optional.ofNullable(userRepository.findByEmail(email));
	}

	// Find user by ID
	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}
}
