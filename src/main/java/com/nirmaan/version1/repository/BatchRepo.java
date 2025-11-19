package com.nirmaan.version1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nirmaan.version1.entity.Batch;
import com.nirmaan.version1.entity.Batch.BatchStatus;

@Repository
public interface BatchRepo extends JpaRepository<Batch, Integer> {

	// Find batch by code
	Optional<Batch> findByBatchCode(String batchCode);

	// Check if batch code exists
	boolean existsByBatchCode(String batchCode);

	// Find batches by name (case-insensitive search)
	List<Batch> findByBatchNameContainingIgnoreCase(String name);

	// Find batches by status
	List<Batch> findByStatus(BatchStatus status);

	// Find active batches
	@Query("SELECT b FROM Batch b WHERE b.status = 'ACTIVE'")
	List<Batch> findActiveBatches();

	// Find batches with available slots
	@Query("SELECT b FROM Batch b WHERE SIZE(b.students) < b.maxCount AND b.status = 'ACTIVE'")
	List<Batch> findBatchesWithAvailableSlots();

	// Find full batches
	@Query("SELECT b FROM Batch b WHERE SIZE(b.students) >= b.maxCount")
	List<Batch> findFullBatches();

	// Count batches by status
	long countByStatus(BatchStatus status);

	// Get batch with student count - FIXED: More efficient query
	@Query("SELECT b FROM Batch b LEFT JOIN FETCH b.students WHERE b.batchId = :batchId")
	Optional<Batch> findByIdWithStudents(@Param("batchId") Integer batchId);

	// NEW: Find all batches with students (prevents N+1)
	@Query("SELECT DISTINCT b FROM Batch b LEFT JOIN FETCH b.students")
	List<Batch> findAllWithStudents();

	// NEW: Get direct count of students in a batch from database
	@Query("SELECT COUNT(s) FROM Student s WHERE s.batch.batchId = :batchId")
	Integer getStudentCountInBatch(@Param("batchId") Integer batchId);
}