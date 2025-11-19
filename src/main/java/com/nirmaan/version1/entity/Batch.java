package com.nirmaan.version1.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode; // Add this import for completeness
import lombok.ToString; // Add this import for completeness
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "batches", indexes = { @Index(name = "idx_batch_code", columnList = "batchCode"),
		@Index(name = "idx_batch_name", columnList = "batchName") })
public class Batch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer batchId;

	@NotBlank(message = "Batch name is required")
	@Size(min = 2, max = 100, message = "Batch name must be between 2 and 100 characters")
	@Column(nullable = false, length = 100)
	private String batchName;

	@NotBlank(message = "Batch code is required")
	@Size(min = 2, max = 20, message = "Batch code must be between 2 and 20 characters")
	@Column(nullable = false, unique = true, length = 20)
	private String batchCode;

	@Min(value = 1, message = "Maximum count must be at least 1")
	@Column(nullable = false)
	private Integer maxCount;

	@Column(length = 255)
	private String description;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private BatchStatus status = BatchStatus.ACTIVE;

	@Column(updatable = false)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	// One batch can have many students
	@OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	private List<Student> students = new ArrayList<>();

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	// Get current student count
	@Transient
	public Integer getCurrentCount() {
		return students != null ? students.size() : 0;
	}

	// Check if batch is full
	@Transient
	public boolean isFull() {
		return getCurrentCount() >= maxCount;
	}

	// Get available slots
	@Transient
	public Integer getAvailableSlots() {
		return maxCount - getCurrentCount();
	}

	public enum BatchStatus {
		ACTIVE, INACTIVE, COMPLETED, UPCOMING
	}
}