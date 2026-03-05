package com.yuosef.cloudbasedlabexaminationplatform.models.Dtos;


import com.yuosef.cloudbasedlabexaminationplatform.models.User;

import java.util.Date;

public record ResponseTemplateDto(Long Id,
                                  String amiName,
                                  String amiId,
                                  Date createdAt,
                                  User createdBy) {
}
