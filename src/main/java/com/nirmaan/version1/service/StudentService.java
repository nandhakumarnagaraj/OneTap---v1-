package com.nirmaan.version1.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nirmaan.version1.dto.AttendanceSummary;
import com.nirmaan.version1.dto.StudentCreateRequest;
import com.nirmaan.version1.dto.StudentResponse;
import com.nirmaan.version1.entity.Batch;
import com.nirmaan.version1.entity.Student;
import com.nirmaan.version1.entity.Student.AttendanceStatus;
import com.nirmaan.version1.exception.DuplicateResourceException;
import com.nirmaan.version1.exception.InvalidOperationException;
import com.nirmaan.version1.exception.ResourceNotFoundException;
import com.nirmaan.version1.repository.StudentRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class StudentService {

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private BatchService batchService;
    

    /**
     * Create new student with optional batch assignment
     * @param request Student creation request
     * @return StudentResponse with created student details
     */
    public StudentResponse createStudent(StudentCreateRequest request) {
        log.info("Creating new student with roll number: {}", request.getRollNumber());

        // Check if roll number already exists
        if (studentRepo.existsByRollNumber(request.getRollNumber())) {
            throw new DuplicateResourceException(
                "Student with roll number " + request.getRollNumber() + " already exists");
        }

        Student student = Student.builder()
            .sname(request.getSname())
            .email(request.getEmail())
            .phone(request.getPhone())
            .rollNumber(request.getRollNumber())
            .status(AttendanceStatus.ABSENT)
            .build();

        // Assign to batch if batchId provided
        if (request.getBatchId() != null) {
            Batch batch = batchService.findBatchById(request.getBatchId());

            // Check if batch is full
            if (batch.isFull()) {
                throw new InvalidOperationException(
                    "Batch " + batch.getBatchCode() + " is full. Maximum capacity: " + batch.getMaxCount());
            }

            student.setBatch(batch);
            log.info("Student assigned to batch: {}", batch.getBatchCode());
        }

        Student saved = studentRepo.save(student);
        log.info("Student created successfully with ID: {}", saved.getSid());

        return mapToResponse(saved);
    }

    /**
     * Get student by ID
     * @param sid Student ID
     * @return StudentResponse with student details
     */
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Integer sid) {
        log.info("Fetching student with ID: {}", sid);
        Student student = findStudentById(sid);
        
        // Initialize batch if present (prevents LazyInitializationException)
        if (student.getBatch() != null) {
            student.getBatch().getBatchName(); // Force initialization
        }
        
        return mapToResponse(student);
    }

    /**
     * Get all students
     * @return List of all students
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        log.info("Fetching all students");
        List<Student> students = studentRepo.findAll();
        
        // Force batch initialization for all students
        students.forEach(s -> {
            if (s.getBatch() != null) {
                s.getBatch().getBatchName();
            }
        });
        
        return students.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Check-in student with late detection
     * @param sid Student ID
     * @return StudentResponse with updated check-in details
     */
    public StudentResponse checkIn(Integer sid) {
        log.info("Processing check-in for student ID: {}", sid);

        Student student = findStudentById(sid);

        // Validate check-in
        if (student.isCheckedIn()) {
            throw new InvalidOperationException("Student is already checked in");
        }

        // Check if already checked in today
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        if (student.getIntime() != null && 
            student.getIntime().isAfter(todayStart) && 
            student.getOuttime() != null) {
            throw new InvalidOperationException("Student has already completed attendance for today");
        }

        LocalDateTime now = LocalDateTime.now();
        student.setIntime(now);
        student.setOuttime(null); // Reset checkout time

        // Determine if late (after 9 AM)
        if (now.toLocalTime().isAfter(LocalTime.of(9, 0))) {
            student.setStatus(AttendanceStatus.LATE);
            log.warn("Student {} checked in late at {}", sid, now);
        } else {
            student.setStatus(AttendanceStatus.PRESENT);
        }

        Student updated = studentRepo.save(student);
        log.info("Student {} checked in successfully at {}", sid, now);

        return mapToResponse(updated);
    }

    /**
     * Check-out student with hours calculation
     * @param sid Student ID
     * @return StudentResponse with updated check-out details
     */
    public StudentResponse checkOut(Integer sid) {
        log.info("Processing check-out for student ID: {}", sid);

        Student student = findStudentById(sid);

        // Validate check-out
        if (!student.isCheckedIn()) {
            throw new InvalidOperationException("Student must check in before checking out");
        }

        if (student.getOuttime() != null) {
            throw new InvalidOperationException("Student has already checked out");
        }

        LocalDateTime now = LocalDateTime.now();
        student.setOuttime(now);

        Student updated = studentRepo.save(student);
        log.info("Student {} checked out successfully at {}. Total hours: {}", 
            sid, now, updated.getHoursPresent());

        return mapToResponse(updated);
    }

    /**
     * Update student details including batch transfer
     * @param sid Student ID
     * @param request Updated student details
     * @return StudentResponse with updated details
     */
    public StudentResponse updateStudent(Integer sid, StudentCreateRequest request) {
        log.info("Updating student with ID: {}", sid);

        Student student = findStudentById(sid);

        // Check if new roll number conflicts with another student
        if (!student.getRollNumber().equals(request.getRollNumber()) && 
            studentRepo.existsByRollNumber(request.getRollNumber())) {
            throw new DuplicateResourceException("Roll number already exists");
        }

        student.setSname(request.getSname());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setRollNumber(request.getRollNumber());

        // Update batch if provided
        if (request.getBatchId() != null) {
            Batch newBatch = batchService.findBatchById(request.getBatchId());

            // If changing batch, check if new batch is full
            if (student.getBatch() == null || 
                !student.getBatch().getBatchId().equals(request.getBatchId())) {
                
                if (newBatch.isFull()) {
                    throw new InvalidOperationException(
                        "Batch " + newBatch.getBatchCode() + " is full");
                }
                
                log.info("Transferring student {} from batch {} to batch {}", 
                    sid, 
                    student.getBatch() != null ? student.getBatch().getBatchCode() : "none",
                    newBatch.getBatchCode());
                
                student.setBatch(newBatch);
            }
        } else {
            // If batchId is explicitly null, remove from batch
            if (student.getBatch() != null) {
                log.info("Removing student {} from batch {}", 
                    sid, student.getBatch().getBatchCode());
                student.setBatch(null);
            }
        }

        Student updated = studentRepo.save(student);
        log.info("Student {} updated successfully", sid);

        return mapToResponse(updated);
    }

    /**
     * Delete student
     * @param sid Student ID
     */
    public void deleteStudent(Integer sid) {
        log.info("Deleting student with ID: {}", sid);

        if (!studentRepo.existsById(sid)) {
            throw new ResourceNotFoundException("Student not found with ID: " + sid);
        }

        studentRepo.deleteById(sid);
        log.info("Student {} deleted successfully", sid);
    }

    /**
     * Search students by name (case-insensitive)
     * @param name Search term
     * @return List of matching students
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> searchByName(String name) {
        log.info("Searching students by name: {}", name);
        List<Student> students = studentRepo.findBySnameContainingIgnoreCase(name);
        
        // Force batch initialization
        students.forEach(s -> {
            if (s.getBatch() != null) {
                s.getBatch().getBatchName();
            }
        });
        
        return students.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get currently checked-in students
     * @return List of checked-in students
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> getCurrentlyCheckedIn() {
        log.info("Fetching currently checked-in students");
        List<Student> students = studentRepo.findCurrentlyCheckedIn();
        
        // Force batch initialization
        students.forEach(s -> {
            if (s.getBatch() != null) {
                s.getBatch().getBatchName();
            }
        });
        
        return students.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get students present today
     * @return List of students present today
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> getPresentToday() {
        log.info("Fetching students present today");
        List<Student> students = studentRepo.findPresentToday();
        
        // Force batch initialization
        students.forEach(s -> {
            if (s.getBatch() != null) {
                s.getBatch().getBatchName();
            }
        });
        
        return students.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get students by batch ID
     * @param batchId Batch ID
     * @return List of students in the batch
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByBatchId(Integer batchId) {
        log.info("Fetching students for batch ID: {}", batchId);
        
        // Verify batch exists
        batchService.findBatchById(batchId);
        
        List<Student> students = studentRepo.findByBatch_BatchId(batchId);
        
        return students.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get attendance summary for a student
     * @param sid Student ID
     * @return AttendanceSummary with statistics
     */
    @Transactional(readOnly = true)
    public AttendanceSummary getAttendanceSummary(Integer sid) {
        log.info("Generating attendance summary for student ID: {}", sid);

        Student student = findStudentById(sid);

        long presentDays = studentRepo.countAttendanceByStatus(sid, AttendanceStatus.PRESENT) +
                          studentRepo.countAttendanceByStatus(sid, AttendanceStatus.LATE);
        long absentDays = studentRepo.countAttendanceByStatus(sid, AttendanceStatus.ABSENT);
        long totalDays = presentDays + absentDays;

        double attendancePercentage = totalDays > 0 ? (presentDays * 100.0 / totalDays) : 0.0;

        return AttendanceSummary.builder()
                .sid(student.getSid())
                .sname(student.getSname())
                .rollNumber(student.getRollNumber())
                .totalDaysPresent(presentDays)
                .totalDaysAbsent(absentDays)
                .attendancePercentage(attendancePercentage)
                .build();
    }

    /**
     * Helper method to find student by ID
     * @param sid Student ID
     * @return Student entity
     * @throws ResourceNotFoundException if student not found
     */
    private Student findStudentById(Integer sid) {
        return studentRepo.findById(sid)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + sid));
    }

    /**
     * Map Student entity to StudentResponse DTO
     * @param student Student entity
     * @return StudentResponse DTO
     */
    private StudentResponse mapToResponse(Student student) {
        StudentResponse.StudentResponseBuilder builder = StudentResponse.builder()
            .sid(student.getSid())
            .sname(student.getSname())
            .email(student.getEmail())
            .phone(student.getPhone())
            .rollNumber(student.getRollNumber())
            .intime(student.getIntime())
            .outtime(student.getOuttime())
            .status(student.getStatus().name())
            .hoursPresent(student.getHoursPresent())
            .checkedIn(student.isCheckedIn())
            .createdAt(student.getCreatedAt());

        // Safely access batch information
        if (student.getBatch() != null) {
            try {
                builder
                    .batchId(student.getBatch().getBatchId())
                    .batchName(student.getBatch().getBatchName())
                    .batchCode(student.getBatch().getBatchCode());
            } catch (Exception e) {
                log.warn("Could not load batch information for student {}: {}", 
                    student.getSid(), e.getMessage());
                // Batch fields will remain null
            }
        }

        return builder.build();
    }
}