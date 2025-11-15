package com.nirmaan.version1.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

// Request DTO for creating batch
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateRequest {
    
    @NotBlank(message = "Batch name is required")
    @Size(min = 2, max = 100, message = "Batch name must be between 2 and 100 characters")
    private String batchName;
    
    @NotBlank(message = "Batch code is required")
    @Size(min = 2, max = 20, message = "Batch code must be between 2 and 20 characters")
    private String batchCode;
    
    @Min(value = 1, message = "Maximum count must be at least 1")
    private Integer maxCount;
    
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

