package com.nirmaan.version1.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//Response DTO for batch
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {
	private Integer batchId;
	private String batchName;
	private String batchCode;
	private Integer maxCount;
	private Integer currentCount;
	private Integer availableSlots;
	private String description;
	private String status;
	private boolean isFull;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
}
