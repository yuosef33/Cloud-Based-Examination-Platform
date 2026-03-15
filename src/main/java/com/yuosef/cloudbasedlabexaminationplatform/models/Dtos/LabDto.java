package com.yuosef.cloudbasedlabexaminationplatform.models.Dtos;

import java.time.LocalDateTime;

public record LabDto(String labName,
                     String labDescription,
                     String labInstructions,
                     Integer labDuration,
                     LocalDateTime labStartTime,
                     Long labTemplateId) {
}
