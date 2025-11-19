package com.nirmaan.version1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nirmaan.version1.repository.UserRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepo userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.debug("Loading user by username: {}", username);

		return userRepo.findByUsername(username).orElseThrow(() -> {
			log.error("User not found: {}", username);
			return new UsernameNotFoundException("User not found: " + username);
		});
	}

	public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
		log.debug("Loading user by email: {}", email);

		return userRepo.findByEmail(email).orElseThrow(() -> {
			log.error("User not found with email: {}", email);
			return new UsernameNotFoundException("User not found with email: " + email);
		});
	}
}