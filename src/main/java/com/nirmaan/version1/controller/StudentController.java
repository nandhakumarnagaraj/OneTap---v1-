package com.nirmaan.version1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nirmaan.version1.dto.ApiResponse;
import com.nirmaan.version1.dto.AttendanceSummary;
import com.nirmaan.version1.dto.StudentCreateRequest;
import com.nirmaan.version1.dto.StudentResponse;
import com.nirmaan.version1.service.StudentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/students")
@Validated
@Slf4j
@CrossOrigin(origins = "*")
public class StudentController {

	@Autowired
	private StudentService studentService;

	/**
	 * Create a new student POST /api/v1/students
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
			@Valid @RequestBody StudentCreateRequest request) {
		log.info("REST request to create student: {}", request.getRollNumber());

		StudentResponse response = studentService.createStudent(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Student created successfully", response));
	}

	/**
	 * Get student by ID GET /api/v1/students/{id}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<StudentResponse>> getStudent(@PathVariable @Min(1) Integer id) {
		log.info("REST request to get student: {}", id);

		StudentResponse response = studentService.getStudentById(id);
		return ResponseEntity.ok(ApiResponse.success("Student retrieved successfully", response));
	}

	/**
	 * Get all students GET /api/v1/students
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents() {
		log.info("REST request to get all students");

		List<StudentResponse> students = studentService.getAllStudents();
		return ResponseEntity.ok(ApiResponse.success("Retrieved " + students.size() + " students", students));
	}

	/**
	 * Update student details PUT /api/v1/students/{id}
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(@PathVariable @Min(1) Integer id,
			@Valid @RequestBody StudentCreateRequest request) {
		log.info("REST request to update student: {}", id);

		StudentResponse response = studentService.updateStudent(id, request);
		return ResponseEntity.ok(ApiResponse.success("Student updated successfully", response));
	}

	/**
	 * Delete student DELETE /api/v1/students/{id}
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable @Min(1) Integer id) {
		log.info("REST request to delete student: {}", id);

		studentService.deleteStudent(id);
		return ResponseEntity.ok(ApiResponse.success("Student deleted successfully", null));
	}

	/**
	 * Check-in student PATCH /api/v1/students/{id}/checkin
	 */
	@PatchMapping("/{id}/checkin")
	public ResponseEntity<ApiResponse<StudentResponse>> checkIn(@PathVariable @Min(1) Integer id) {
		log.info("REST request to check-in student: {}", id);

		StudentResponse response = studentService.checkIn(id);
		return ResponseEntity.ok(ApiResponse.success("Student checked in successfully", response));
	}

	/**
	 * Check-out student PATCH /api/v1/students/{id}/checkout
	 */
	@PatchMapping("/{id}/checkout")
	public ResponseEntity<ApiResponse<StudentResponse>> checkOut(@PathVariable @Min(1) Integer id) {
		log.info("REST request to check-out student: {}", id);

		StudentResponse response = studentService.checkOut(id);
		return ResponseEntity.ok(ApiResponse.success("Student checked out successfully", response));
	}

	/**
	 * Search students by name GET /api/v1/students/search?name={name}
	 */
	@GetMapping("/search")
	public ResponseEntity<ApiResponse<List<StudentResponse>>> searchStudents(@RequestParam String name) {
		log.info("REST request to search students by name: {}", name);

		List<StudentResponse> students = studentService.searchByName(name);
		return ResponseEntity.ok(ApiResponse.success("Found " + students.size() + " students", students));
	}

	/**
	 * Get currently checked-in students GET /api/v1/students/checked-in
	 */
	@GetMapping("/checked-in")
	public ResponseEntity<ApiResponse<List<StudentResponse>>> getCurrentlyCheckedIn() {
		log.info("REST request to get currently checked-in students");

		List<StudentResponse> students = studentService.getCurrentlyCheckedIn();
		return ResponseEntity.ok(ApiResponse.success("Found " + students.size() + " checked-in students", students));
	}

	/**
	 * Get students present today GET /api/v1/students/present-today
	 */
	@GetMapping("/present-today")
	public ResponseEntity<ApiResponse<List<StudentResponse>>> getPresentToday() {
		log.info("REST request to get students present today");

		List<StudentResponse> students = studentService.getPresentToday();
		return ResponseEntity.ok(ApiResponse.success("Found " + students.size() + " students present today", students));
	}

	/**
	 * Get students by batch ID GET /api/v1/students/batch/{batchId}
	 */
	@GetMapping("/batch/{batchId}")
	public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentsByBatch(
			@PathVariable @Min(1) Integer batchId) {
		log.info("REST request to get students for batch: {}", batchId);

		List<StudentResponse> students = studentService.getStudentsByBatchId(batchId);
		return ResponseEntity.ok(ApiResponse.success("Found " + students.size() + " students in batch", students));
	}

	/**
	 * Get attendance summary for a student GET
	 * /api/v1/students/{id}/attendance-summary
	 */
	@GetMapping("/{id}/attendance-summary")
	public ResponseEntity<ApiResponse<AttendanceSummary>> getAttendanceSummary(@PathVariable @Min(1) Integer id) {
		log.info("REST request to get attendance summary for student: {}", id);

		AttendanceSummary summary = studentService.getAttendanceSummary(id);
		return ResponseEntity.ok(ApiResponse.success("Attendance summary retrieved successfully", summary));
	}
}