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

	// Find student by roll number
	Optional<Student> findByRollNumber(String rollNumber);

	// Find students by name (case-insensitive search)
	List<Student> findBySnameContainingIgnoreCase(String name);

	// Find students by status
	List<Student> findByStatus(AttendanceStatus status);

	// Find students checked in (have intime but no outtime)
	@Query("SELECT s FROM Student s WHERE s.intime IS NOT NULL AND s.outtime IS NULL")
	List<Student> findCurrentlyCheckedIn();

	// Find students by date range
	@Query("SELECT s FROM Student s WHERE s.intime BETWEEN :startDate AND :endDate")
	List<Student> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	// Count students by status
	long countByStatus(AttendanceStatus status);

	// Find students present today
	@Query("SELECT s FROM Student s WHERE DATE(s.intime) = CURRENT_DATE")
	List<Student> findPresentToday();

	// Check if roll number exists
	boolean existsByRollNumber(String rollNumber);

	// Get attendance summary for a student
	@Query("SELECT COUNT(s) FROM Student s WHERE s.sid = :sid AND s.status = :status")
	long countAttendanceByStatus(@Param("sid") Integer sid, @Param("status") AttendanceStatus status);

	// Find late check-ins (after 9 AM)
	@Query("SELECT s FROM Student s WHERE HOUR(s.intime) > 9 AND DATE(s.intime) = CURRENT_DATE")
	List<Student> findLateArrivals();
}