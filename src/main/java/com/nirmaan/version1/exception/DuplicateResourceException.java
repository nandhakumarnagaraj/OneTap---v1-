package com.nirmaan.version1.exception;

//Custom exception for duplicate resources
public class DuplicateResourceException extends RuntimeException {
	public DuplicateResourceException(String message) {
		super(message);
	}
}
