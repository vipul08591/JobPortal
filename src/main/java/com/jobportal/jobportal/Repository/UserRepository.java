package com.jobportal.jobportal.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobportal.jobportal.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	User findByEmail(String email);
}
