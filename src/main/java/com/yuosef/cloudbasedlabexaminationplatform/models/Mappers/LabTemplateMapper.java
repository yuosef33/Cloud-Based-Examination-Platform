package com.yuosef.cloudbasedlabexaminationplatform.models.Mappers;

import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.LabTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;

public class LabTemplateMapper {
    public static LabTemplateDto toDto(LabTemplate labTemplate) {
        return new LabTemplateDto(
                labTemplate.getId(),
                labTemplate.getAmiName(),
                labTemplate.getAmiId(),
                labTemplate.getCreatedAt(),
                labTemplate.getCreatedBy().getId()
        );
    }
}