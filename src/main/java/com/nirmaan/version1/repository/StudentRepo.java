package com.nirmaan.version1.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nirmaan.version1.entity.Student;
import com.nirmaan.version1.entity.Student.AttendanceStatus;

@Repository
public interface StudentRepo extends JpaRepository<Student, Integer> {

	// Find student by roll number with batch
	@Query("SELECT s FROM Student s LEFT JOIN FETCH s.batch WHERE s.rollNumber = :rollNumber")
	Optional<Student> findByRollNumber(@Param("rollNumber") String rollNumber);

	// Find students by name with batch (FIXED - Added JOIN FETCH)
	@Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.batch WHERE LOWER(s.sname) LIKE LOWER(CONCAT('%', :name, '%'))")
	List<Student> findBySnameContainingIgnoreCase(@Param("name") String name);

	// Find students by status with batch (FIXED - Added JOIN FETCH)
	@Query("SELECT s FROM Student s LEFT JOIN FETCH s.batch WHERE s.status = :status")
	List<Student> findByStatus(@Param("status") AttendanceStatus status);

	// Find students checked in with batch (FIXED - Added JOIN FETCH)
	@Query("SELECT s FROM Student s LEFT JOIN FETCH s.batch WHERE s.intime IS NOT NULL AND s.outtime IS NULL")
	List<Student> findCurrentlyCheckedIn();

	// Find students by date range with batch (FIXED - Added JOIN FETCH)
	@Query("SELECT s FROM Student s LEFT JOIN FETCH s.batch WHERE s.intime BETWEEN :startDate AND :endDate")
	List<Student> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	// Count students by status (no change needed)
	long countByStatus(AttendanceStatus status);

	// Find students present today with batch (FIXED - Added JOIN FETCH)
	@Query("SELECT s FROM Student s LEFT JOIN FETCH s.batch WHERE DATE(s.intime) = CURRENT_DATE")
	List<Student> findPresentToday();

	// Check if roll number exists (no change needed)
	boolean existsByRollNumber(String rollNumber);

	// Get attendance summary for a student (no change needed)
	@Query("SELECT COUNT(s) FROM Student s WHERE s.sid = :sid AND s.status = :status")
	long countAttendanceByStatus(@Param("sid") Integer sid, @Param("status") AttendanceStatus status);

	// Find late check-ins with batch (FIXED - Added JOIN FETCH)
	@Query("SELECT s FROM Student s LEFT JOIN FETCH s.batch WHERE HOUR(s.intime) > 9 AND DATE(s.intime) = CURRENT_DATE")
	List<Student> findLateArrivals();

	// Find students by batch ID with batch (FIXED - Added JOIN FETCH)
	@Query("SELECT s FROM Student s LEFT JOIN FETCH s.batch b WHERE b.batchId = :batchId")
	List<Student> findByBatch_BatchId(@Param("batchId") Integer batchId);

	// Find all students with batch (NEW - Most Important Fix)
	@Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.batch")
	List<Student> findAllWithBatch();
}