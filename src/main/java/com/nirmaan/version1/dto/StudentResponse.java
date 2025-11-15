package com.nirmaan.version1.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Student Includes batch information for students assigned to
 * batches
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

	// Student basic information
	private Integer sid;
	private String sname;
	private String email;
	private String phone;
	private String rollNumber;

	// Attendance information
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime intime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime outtime;

	private String status;
	private Double hoursPresent;
	private boolean checkedIn;

	// Metadata
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	// Batch information (null if student not assigned to any batch)
	private Integer batchId;
	private String batchName;
	private String batchCode;
}