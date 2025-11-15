package com.nirmaan.version1.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//Response DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
	private Integer sid;
	private String sname;
	private String email;
	private String phone;
	private String rollNumber;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime intime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime outtime;

	private String status;
	private Double hoursPresent;
	private boolean checkedIn;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
}
