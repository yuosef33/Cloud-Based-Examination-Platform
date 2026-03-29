package com.yuosef.cloudbasedlabexaminationplatform.models.Mappers;


import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.LabDtoResponse;
import com.yuosef.cloudbasedlabexaminationplatform.models.Lab;


public class LabMapper {
    public static LabDtoResponse toDto(Lab lab) {
        return new LabDtoResponse(lab.getId(),
                lab.getLabName(),
                lab.getLabDescription(),
                lab.getLabInstructions(),
                Integer.parseInt(String.valueOf(lab.getLabDuration().toMinutes())),
                lab.getLabStartTime(),
                lab.getLabEndTime(),
                lab.getLabTemplate().getId(),
                lab.getStatus()
        );
    }

}
