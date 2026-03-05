package com.yuosef.demo1.cloudexaminationplatform.Models.Dtos;


import java.time.Duration;
import java.time.LocalDateTime;

public record LabDto(String labName,
                     Duration labDuration,
                     LocalDateTime labStartTime,
                     LocalDateTime labEndTime,
                     String labTemplate) {
}
