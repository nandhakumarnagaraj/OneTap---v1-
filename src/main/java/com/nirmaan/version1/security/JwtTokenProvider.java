package com.nirmaan.version1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenProvider {

	@Value("${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidationPurposesOnlyForSAMS123456789}")
	private String jwtSecret;

	@Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
	private long jwtExpirationMs;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}

	/**
	 * Generate JWT token from UserDetails
	 */
	public String generateToken(UserDetails userDetails, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);
		return createToken(claims, userDetails.getUsername());
	}

	/**
	 * Generate JWT token with additional claims
	 */
	public String generateToken(UserDetails userDetails, String role, Integer userId) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);
		claims.put("userId", userId);
		return createToken(claims, userDetails.getUsername());
	}

	/**
	 * Create JWT token
	 */
	private String createToken(Map<String, Object> claims, String subject) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(now).setExpiration(expiryDate)
				.signWith(getSigningKey(), SignatureAlgorithm.HS512).compact();
	}

	/**
	 * Get username from token
	 */
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	/**
	 * Get role from token
	 */
	public String getRoleFromToken(String token) {
		return (String) getAllClaimsFromToken(token).get("role");
	}

	/**
	 * Get userId from token
	 */
	public Integer getUserIdFromToken(String token) {
		return (Integer) getAllClaimsFromToken(token).get("userId");
	}

	/**
	 * Get expiration date from token
	 */
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	/**
	 * Get specific claim from token
	 */
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Get all claims from token
	 */
	private Claims getAllClaimsFromToken(String token) {
		try {
			return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
		} catch (Exception e) {
			log.error("Error parsing JWT token: {}", e.getMessage());
			throw new RuntimeException("Invalid JWT token", e);
		}
	}

	/**
	 * Check if token is expired
	 */
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	/**
	 * Validate token
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	/**
	 * Validate token without UserDetails (basic validation)
	 */
	public Boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
			return !isTokenExpired(token);
		} catch (Exception e) {
			log.error("JWT validation failed: {}", e.getMessage());
			return false;
		}
	}
}