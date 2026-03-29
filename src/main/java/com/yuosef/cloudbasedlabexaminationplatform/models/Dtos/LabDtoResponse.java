package com.yuosef.cloudbasedlabexaminationplatform.models.Dtos;

import com.yuosef.cloudbasedlabexaminationplatform.models.LabStatus;

import java.time.LocalDateTime;

public record LabDtoResponse(Long labId,
                             String labName,
                             String labDescription,
                             String labInstructions,
                             Integer labDuration,
                             LocalDateTime labStartTime,
                             LocalDateTime labEndTime,
                             Long labTemplateId,
                             LabStatus labStatus) {
}
