package com.yuosef.cloudbasedlabexaminationplatform.models.Dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileDto {
    private String fileName;
    private String downloadUrl;
    private Long size;
}