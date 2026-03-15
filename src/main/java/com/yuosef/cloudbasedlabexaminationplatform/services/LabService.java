package com.yuosef.cloudbasedlabexaminationplatform.services;

import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.LabDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.LabTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.RequestTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import jakarta.transaction.SystemException;

import java.util.List;


public interface LabService {

     Object createLab(LabDto labDto) throws SystemException;
    LabTemplateDto createTemplate(RequestTemplateDto requestTemplateDto);
     List<LabTemplate> getAlltemplatesByUserId();
}
