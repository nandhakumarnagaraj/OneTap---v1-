package com.nirmaan.version1.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//Attendance Summary DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummary {
	private Integer sid;
	private String sname;
	private String rollNumber;
	private long totalDaysPresent;
	private long totalDaysAbsent;
	private double averageHoursPerDay;
	private double attendancePercentage;
}
