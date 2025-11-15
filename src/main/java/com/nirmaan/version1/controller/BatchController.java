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
import com.nirmaan.version1.dto.BatchCreateRequest;
import com.nirmaan.version1.dto.BatchResponse;
import com.nirmaan.version1.dto.BatchSummary;
import com.nirmaan.version1.entity.Batch.BatchStatus;
import com.nirmaan.version1.service.BatchService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/batches")
@Validated
@Slf4j
@CrossOrigin(origins = "*")
public class BatchController {

    @Autowired
    private BatchService batchService;

    /**
     * Create a new batch
     * POST /api/v1/batches
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BatchResponse>> createBatch(
            @Valid @RequestBody BatchCreateRequest request) {
        log.info("REST request to create batch: {}", request.getBatchCode());
        
        BatchResponse response = batchService.createBatch(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch created successfully", response));
    }

    /**
     * Get batch by ID
     * GET /api/v1/batches/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> getBatch(
            @PathVariable @Min(1) Integer id) {
        log.info("REST request to get batch: {}", id);
        
        BatchResponse response = batchService.getBatchById(id);
        return ResponseEntity.ok(ApiResponse.success("Batch retrieved successfully", response));
    }

    /**
     * Get all batches
     * GET /api/v1/batches
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BatchResponse>>> getAllBatches() {
        log.info("REST request to get all batches");
        
        List<BatchResponse> batches = batchService.getAllBatches();
        return ResponseEntity.ok(
            ApiResponse.success("Retrieved " + batches.size() + " batches", batches));
    }

    /**
     * Update batch details
     * PUT /api/v1/batches/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> updateBatch(
            @PathVariable @Min(1) Integer id,
            @Valid @RequestBody BatchCreateRequest request) {
        log.info("REST request to update batch: {}", id);
        
        BatchResponse response = batchService.updateBatch(id, request);
        return ResponseEntity.ok(ApiResponse.success("Batch updated successfully", response));
    }

    /**
     * Delete batch
     * DELETE /api/v1/batches/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(
            @PathVariable @Min(1) Integer id) {
        log.info("REST request to delete batch: {}", id);
        
        batchService.deleteBatch(id);
        return ResponseEntity.ok(ApiResponse.success("Batch deleted successfully", null));
    }

    /**
     * Update batch status
     * PATCH /api/v1/batches/{id}/status?status=ACTIVE
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BatchResponse>> updateBatchStatus(
            @PathVariable @Min(1) Integer id,
            @RequestParam BatchStatus status) {
        log.info("REST request to update batch {} status to {}", id, status);
        
        BatchResponse response = batchService.updateBatchStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Batch status updated successfully", response));
    }

    /**
     * Get active batches
     * GET /api/v1/batches/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BatchResponse>>> getActiveBatches() {
        log.info("REST request to get active batches");
        
        List<BatchResponse> batches = batchService.getActiveBatches();
        return ResponseEntity.ok(
            ApiResponse.success("Found " + batches.size() + " active batches", batches));
    }

    /**
     * Get batches with available slots
     * GET /api/v1/batches/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<BatchResponse>>> getBatchesWithAvailableSlots() {
        log.info("REST request to get batches with available slots");
        
        List<BatchResponse> batches = batchService.getBatchesWithAvailableSlots();
        return ResponseEntity.ok(
            ApiResponse.success("Found " + batches.size() + " batches with available slots", batches));
    }

    /**
     * Search batches by name
     * GET /api/v1/batches/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BatchResponse>>> searchBatches(
            @RequestParam String name) {
        log.info("REST request to search batches by name: {}", name);
        
        List<BatchResponse> batches = batchService.searchByName(name);
        return ResponseEntity.ok(
            ApiResponse.success("Found " + batches.size() + " batches", batches));
    }

    /**
     * Get batch summary with attendance stats
     * GET /api/v1/batches/{id}/summary
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<BatchSummary>> getBatchSummary(
            @PathVariable @Min(1) Integer id) {
        log.info("REST request to get batch summary for: {}", id);
        
        BatchSummary summary = batchService.getBatchSummary(id);
        return ResponseEntity.ok(
            ApiResponse.success("Batch summary retrieved successfully", summary));
    }
}