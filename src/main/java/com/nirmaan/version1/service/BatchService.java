package com.nirmaan.version1.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nirmaan.version1.dto.BatchCreateRequest;
import com.nirmaan.version1.dto.BatchResponse;
import com.nirmaan.version1.dto.BatchSummary;
import com.nirmaan.version1.entity.Batch;
import com.nirmaan.version1.entity.Batch.BatchStatus;
import com.nirmaan.version1.entity.Student;
import com.nirmaan.version1.entity.Student.AttendanceStatus;
import com.nirmaan.version1.exception.DuplicateResourceException;
import com.nirmaan.version1.exception.ResourceNotFoundException;
import com.nirmaan.version1.repository.BatchRepo;
import com.nirmaan.version1.repository.StudentRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class BatchService {

	@Autowired
	private BatchRepo batchRepo;

	@Autowired
	private StudentRepo studentRepo;

	// Create new batch
	public BatchResponse createBatch(BatchCreateRequest request) {
		log.info("Creating new batch with code: {}", request.getBatchCode());

		// Check if batch code already exists
		if (batchRepo.existsByBatchCode(request.getBatchCode())) {
			throw new DuplicateResourceException("Batch with code " + request.getBatchCode() + " already exists");
		}

		Batch batch = Batch.builder().batchName(request.getBatchName()).batchCode(request.getBatchCode())
				.maxCount(request.getMaxCount()).description(request.getDescription()).startDate(request.getStartDate())
				.endDate(request.getEndDate()).status(BatchStatus.ACTIVE).build();

		Batch saved = batchRepo.save(batch);
		log.info("Batch created successfully with ID: {}", saved.getBatchId());

		return mapToResponse(saved);
	}

	// Get batch by ID
	@Transactional(readOnly = true)
	public BatchResponse getBatchById(Integer batchId) {
		log.info("Fetching batch with ID: {}", batchId);
		Batch batch = findBatchById(batchId);
		return mapToResponse(batch);
	}

	// Get all batches
	@Transactional(readOnly = true)
	public List<BatchResponse> getAllBatches() {
		log.info("Fetching all batches");
		return batchRepo.findAllWithStudents().stream().map(this::mapToResponse).collect(Collectors.toList());
	}

	// Update batch
	public BatchResponse updateBatch(Integer batchId, BatchCreateRequest request) {
		log.info("Updating batch with ID: {}", batchId);

		Batch batch = findBatchById(batchId);

		// Check if new batch code conflicts with another batch
		if (!batch.getBatchCode().equals(request.getBatchCode())
				&& batchRepo.existsByBatchCode(request.getBatchCode())) {
			throw new DuplicateResourceException("Batch code already exists");
		}

		// Get current student count from database (not from loaded students list)
		Integer currentStudentCount = getStudentCountForBatch(batchId);

		// Don't allow reducing maxCount below current student count
		if (request.getMaxCount() < currentStudentCount) {
			throw new IllegalArgumentException(
					"Cannot reduce max count below current student count (" + currentStudentCount + ")");
		}

		batch.setBatchName(request.getBatchName());
		batch.setBatchCode(request.getBatchCode());
		batch.setMaxCount(request.getMaxCount());
		batch.setDescription(request.getDescription());
		batch.setStartDate(request.getStartDate());
		batch.setEndDate(request.getEndDate());

		Batch updated = batchRepo.save(batch);
		log.info("Batch {} updated successfully", batchId);

		return mapToResponse(updated);
	}

	// Delete batch
	public void deleteBatch(Integer batchId) {
		log.info("Deleting batch with ID: {}", batchId);

		Batch batch = findBatchById(batchId);

		// Check if batch has students
		Integer studentCount = getStudentCountForBatch(batchId);
		if (studentCount > 0) {
			throw new IllegalArgumentException("Cannot delete batch with enrolled students. Remove students first.");
		}

		batchRepo.deleteById(batchId);
		log.info("Batch {} deleted successfully", batchId);
	}

	// Update batch status
	public BatchResponse updateBatchStatus(Integer batchId, BatchStatus status) {
		log.info("Updating batch {} status to {}", batchId, status);

		Batch batch = findBatchById(batchId);
		batch.setStatus(status);

		Batch updated = batchRepo.save(batch);
		return mapToResponse(updated);
	}

	// Get active batches
	@Transactional(readOnly = true)
	public List<BatchResponse> getActiveBatches() {
		log.info("Fetching active batches");
		return batchRepo.findActiveBatches().stream().map(this::mapToResponse).collect(Collectors.toList());
	}

	// Get batches with available slots
	@Transactional(readOnly = true)
	public List<BatchResponse> getBatchesWithAvailableSlots() {
		log.info("Fetching batches with available slots");
		return batchRepo.findBatchesWithAvailableSlots().stream().map(this::mapToResponse).collect(Collectors.toList());
	}

	// Search batches by name
	@Transactional(readOnly = true)
	public List<BatchResponse> searchByName(String name) {
		log.info("Searching batches by name: {}", name);
		return batchRepo.findByBatchNameContainingIgnoreCase(name).stream().map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	// Get batch summary with attendance stats
	@Transactional(readOnly = true)
	public BatchSummary getBatchSummary(Integer batchId) {
		log.info("Generating summary for batch ID: {}", batchId);

		Batch batch = batchRepo.findByIdWithStudents(batchId)
				.orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + batchId));

		List<Student> students = batch.getStudents();
		long presentToday = students.stream()
				.filter(s -> s.getStatus() == AttendanceStatus.PRESENT || s.getStatus() == AttendanceStatus.LATE)
				.count();

		long absentToday = students.stream().filter(s -> s.getStatus() == AttendanceStatus.ABSENT).count();

		double attendancePercentage = students.isEmpty() ? 0.0 : (presentToday * 100.0 / students.size());

		return BatchSummary.builder().batchId(batch.getBatchId()).batchName(batch.getBatchName())
				.batchCode(batch.getBatchCode()).maxCount(batch.getMaxCount()).currentCount(batch.getCurrentCount())
				.availableSlots(batch.getAvailableSlots()).totalPresentToday(presentToday).totalAbsentToday(absentToday)
				.attendancePercentage(attendancePercentage).build();
	}

	// Helper method to find batch by ID with students
	public Batch findBatchById(Integer batchId) {
		return batchRepo.findByIdWithStudents(batchId)
				.orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + batchId));
	}

	/**
	 * Get the actual count of students in a batch directly from database
	 * This bypasses lazy loading issues and gives the true count
	 */
	@Transactional(readOnly = true)
	public Integer getStudentCountForBatch(Integer batchId) {
		Integer count = batchRepo.getStudentCountInBatch(batchId);
		return count != null ? count : 0;
	}

	/**
	 * Map entity to response DTO
	 * IMPORTANT: This method now queries the database for accurate student count
	 */
	private BatchResponse mapToResponse(Batch batch) {
		// Get the actual student count from database to ensure accuracy
		Integer actualStudentCount = getStudentCountForBatch(batch.getBatchId());
		Integer availableSlots = batch.getMaxCount() - actualStudentCount;
		boolean isFull = actualStudentCount >= batch.getMaxCount();

		return BatchResponse.builder()
				.batchId(batch.getBatchId())
				.batchName(batch.getBatchName())
				.batchCode(batch.getBatchCode())
				.maxCount(batch.getMaxCount())
				.currentCount(actualStudentCount)  // Use database count instead of loaded list
				.availableSlots(availableSlots)
				.description(batch.getDescription())
				.status(batch.getStatus().name())
				.isFull(isFull)
				.startDate(batch.getStartDate())
				.endDate(batch.getEndDate())
				.createdAt(batch.getCreatedAt())
				.build();
	}
}