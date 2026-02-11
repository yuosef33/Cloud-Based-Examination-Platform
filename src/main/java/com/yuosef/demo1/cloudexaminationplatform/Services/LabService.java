package com.yuosef.demo1.cloudexaminationplatform.Services;

import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.RequestTemplateDto;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.LabDto;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.ResponseTemplateDto;
import com.yuosef.demo1.cloudexaminationplatform.Models.LabTemplate;

import java.util.List;


public interface LabService {

     Object createLab(LabDto labDto);
     ResponseTemplateDto createTemplate(RequestTemplateDto requestTemplateDto);
     List<LabTemplate> getAlltemplatesByUserId();
}
