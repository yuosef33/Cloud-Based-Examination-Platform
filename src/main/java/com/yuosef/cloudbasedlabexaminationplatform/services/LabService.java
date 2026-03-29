package com.yuosef.cloudbasedlabexaminationplatform.services;

import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.*;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import jakarta.transaction.SystemException;

import java.util.List;


public interface LabService {

     Object createLab(LabDto labDto) throws SystemException;
    List<LabDtoResponse> getalllabs();
    LabTemplateDto createTemplate(RequestTemplateDto requestTemplateDto);
     List<LabTemplate> getAlltemplatesByUserId();
    TerraformOutput createStudentVm(String labId) throws Exception;
    TerraformOutput getStudentVm(String labId) throws SystemException;
}
