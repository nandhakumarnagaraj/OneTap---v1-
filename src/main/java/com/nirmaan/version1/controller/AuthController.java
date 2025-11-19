package com.nirmaan.version1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nirmaan.version1.dto.ApiResponse;
import com.nirmaan.version1.dto.LoginRequest;
import com.nirmaan.version1.dto.LoginResponse;
import com.nirmaan.version1.dto.RegisterRequest;
import com.nirmaan.version1.service.AuthService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {

	@Autowired
	private AuthService authService;

	/**
	 * Register new user POST /api/v1/auth/register
	 */
	@PostMapping("/register")
	public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
		log.info("Register request for username: {}", request.getUsername());

		LoginResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("User registered successfully", response));
	}

	/**
	 * Login user POST /api/v1/auth/login
	 */
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		log.info("Login request for username: {}", request.getUsername());

		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success("User logged in successfully", response));
	}
}