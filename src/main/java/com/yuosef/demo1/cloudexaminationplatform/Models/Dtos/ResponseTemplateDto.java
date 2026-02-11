package com.yuosef.demo1.cloudexaminationplatform.Models.Dtos;

import com.yuosef.demo1.cloudexaminationplatform.Models.User;


import java.util.Date;

public record ResponseTemplateDto(Long Id,
                                  String amiName,
                                  String amiId,
                                  Date createdAt,
                                  User createdBy) {
}
