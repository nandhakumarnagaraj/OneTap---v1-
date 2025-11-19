package com.nirmaan.version1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
	private String token;
	private String tokenType = "Bearer";
	private Integer userId;
	private String username;
	private String email;
	private String role;
}