package com.yuosef.demo1.cloudexaminationplatform.Models.Dtos;

import com.yuosef.demo1.cloudexaminationplatform.Models.LabTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

public record LabDto(String labName,
                     Duration labDuration,
                     LocalDateTime labStartTime,
                     LocalDateTime labEndTime,
                     LabTemplate labTemplate) {
}
