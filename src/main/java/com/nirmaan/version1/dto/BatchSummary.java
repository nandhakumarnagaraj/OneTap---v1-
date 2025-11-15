package com.nirmaan.version1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//Batch summary with student details
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchSummary {
	private Integer batchId;
	private String batchName;
	private String batchCode;
	private Integer maxCount;
	private Integer currentCount;
	private Integer availableSlots;
	private long totalPresentToday;
	private long totalAbsentToday;
	private double attendancePercentage;
}
