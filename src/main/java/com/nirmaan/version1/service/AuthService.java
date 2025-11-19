package com.nirmaan.version1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nirmaan.version1.dto.LoginRequest;
import com.nirmaan.version1.dto.LoginResponse;
import com.nirmaan.version1.dto.RegisterRequest;
import com.nirmaan.version1.entity.User;
import com.nirmaan.version1.entity.User.UserRole;
import com.nirmaan.version1.exception.DuplicateResourceException;
import com.nirmaan.version1.exception.InvalidOperationException;
import com.nirmaan.version1.repository.UserRepo;
import com.nirmaan.version1.security.JwtTokenProvider;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class AuthService {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	/**
	 * Register new user
	 */
	public LoginResponse register(RegisterRequest request) {
		log.info("Registering new user: {}", request.getUsername());

		// Validate passwords match
		if (!request.getPassword().equals(request.getConfirmPassword())) {
			throw new InvalidOperationException("Passwords do not match");
		}

		// Check if username exists
		if (userRepo.existsByUsername(request.getUsername())) {
			throw new DuplicateResourceException("Username already exists");
		}

		// Check if email exists
		if (userRepo.existsByEmail(request.getEmail())) {
			throw new DuplicateResourceException("Email already exists");
		}

		// Determine role
		UserRole role = UserRole.STUDENT;
		try {
			if (request.getRole() != null && !request.getRole().isEmpty()) {
				role = UserRole.valueOf(request.getRole().toUpperCase());
			}
		} catch (IllegalArgumentException e) {
			log.warn("Invalid role provided: {}, defaulting to STUDENT", request.getRole());
		}

		// Create user
		User user = User.builder().username(request.getUsername()).email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword())).role(role).enabled(true)
				.accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(true).build();

		User savedUser = userRepo.save(user);
		log.info("User registered successfully: {}", savedUser.getUsername());

		// Generate token
		String token = jwtTokenProvider.generateToken(savedUser, role.name(), savedUser.getUserId());

		return LoginResponse.builder().token(token).userId(savedUser.getUserId()).username(savedUser.getUsername())
				.email(savedUser.getEmail()).role(role.name()).build();
	}

	/**
	 * Login user
	 */
	public LoginResponse login(LoginRequest request) {
		log.info("User login attempt: {}", request.getUsername());

		// Authenticate
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			User user = (User) authentication.getPrincipal();
			user.setLastLogin(java.time.LocalDateTime.now());
			userRepo.save(user);

			// Generate token
			String token = jwtTokenProvider.generateToken(user, user.getRole().name(), user.getUserId());

			log.info("User logged in successfully: {}", user.getUsername());

			return LoginResponse.builder().token(token).userId(user.getUserId()).username(user.getUsername())
					.email(user.getEmail()).role(user.getRole().name()).build();

		} catch (Exception e) {
			log.error("Authentication failed for user: {}", request.getUsername());
			throw new InvalidOperationException("Invalid username or password");
		}
	}
}
