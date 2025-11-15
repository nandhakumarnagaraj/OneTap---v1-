package com.nirmaan.version1.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.nirmaan.version1.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex,
			WebRequest request) {
		log.error("Resource not found: {}", ex.getMessage());

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message(ex.getMessage())
				.timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(DuplicateResourceException ex,
			WebRequest request) {
		log.error("Duplicate resource: {}", ex.getMessage());

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message(ex.getMessage())
				.timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(response, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(InvalidOperationException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidOperationException(InvalidOperationException ex,
			WebRequest request) {
		log.error("Invalid operation: {}", ex.getMessage());

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message(ex.getMessage())
				.timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
			MethodArgumentNotValidException ex) {
		log.error("Validation error: {}", ex.getMessage());

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder().success(false)
				.message("Validation failed").data(errors).timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex, WebRequest request) {
		log.error("Unexpected error occurred: ", ex);

		ApiResponse<Void> response = ApiResponse.<Void>builder().success(false)
				.message("An unexpected error occurred: " + ex.getMessage()).timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
