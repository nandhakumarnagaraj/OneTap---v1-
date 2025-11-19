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

	@Value("${jwt.secret:MySecureJWTSecretKeyForSAMS2024PleaseChangeThisInProductionEnvironmentWithALongerKey123456789012345}")
	private String jwtSecret;

	@Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
	private long jwtExpirationMs;

	/**
	 * Generate a secure signing key from the secret string
	 * Converts the string to bytes and ensures it meets the 256-bit minimum requirement
	 */
	private SecretKey getSigningKey() {
		byte[] keyBytes = jwtSecret.getBytes();
		
		// Ensure the key is at least 256 bits (32 bytes) for HS256
		// For HS512, we ideally want 512 bits (64 bytes), but 256 bits is the minimum for any HMAC-SHA
		if (keyBytes.length < 32) {
			log.warn("JWT secret is less than 256 bits. Padding with default bytes for security.");
			// This is a fallback - in production, always use a 256+ bit key
			return Keys.hmacShaKeyFor(padKey(keyBytes));
		}
		
		return Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Pad the key to at least 256 bits if it's too short
	 * WARNING: This should only be used as a fallback. Always provide a proper 256+ bit key!
	 */
	private byte[] padKey(byte[] keyBytes) {
		if (keyBytes.length >= 32) {
			return keyBytes;
		}
		
		byte[] paddedKey = new byte[32];
		System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
		
		// Fill remaining with repeating pattern
		for (int i = keyBytes.length; i < 32; i++) {
			paddedKey[i] = (byte) (keyBytes[i % keyBytes.length] ^ 0xAA);
		}
		
		return paddedKey;
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
	 * Create JWT token with HS256 algorithm (more compatible than HS512)
	 */
	private String createToken(Map<String, Object> claims, String subject) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(now)
				.setExpiration(expiryDate)
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Changed from HS512 to HS256
				.compact();
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
		Object userId = getAllClaimsFromToken(token).get("userId");
		if (userId instanceof Integer) {
			return (Integer) userId;
		} else if (userId instanceof Number) {
			return ((Number) userId).intValue();
		}
		return null;
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
			return Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (Exception e) {
			log.error("Error parsing JWT token: {}", e.getMessage());
			throw new RuntimeException("Invalid JWT token", e);
		}
	}

	/**
	 * Check if token is expired
	 */
	private Boolean isTokenExpired(String token) {
		try {
			final Date expiration = getExpirationDateFromToken(token);
			return expiration.before(new Date());
		} catch (Exception e) {
			log.error("Error checking token expiration: {}", e.getMessage());
			return true;
		}
	}

	/**
	 * Validate token with UserDetails
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
			Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token);
			return !isTokenExpired(token);
		} catch (Exception e) {
			log.error("JWT validation failed: {}", e.getMessage());
			return false;
		}
	}
}