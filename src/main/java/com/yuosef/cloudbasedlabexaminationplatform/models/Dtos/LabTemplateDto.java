package com.yuosef.cloudbasedlabexaminationplatform.models.Dtos;

import com.yuosef.cloudbasedlabexaminationplatform.models.OsType;

import java.util.Date;

public record LabTemplateDto(
        Long id,
        String amiName,
        String amiId,
        OsType osType,
        Date createdAt,
        Long createdById  // just the id, not the whole user
) {}