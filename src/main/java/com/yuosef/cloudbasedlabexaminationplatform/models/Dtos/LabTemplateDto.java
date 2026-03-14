package com.yuosef.cloudbasedlabexaminationplatform.models.Dtos;

import java.util.Date;

public record LabTemplateDto(
        Long id,
        String amiName,
        String amiId,
        Date createdAt,
        Long createdById  // just the id, not the whole user
) {}