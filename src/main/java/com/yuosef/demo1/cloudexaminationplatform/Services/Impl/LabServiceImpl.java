package com.yuosef.demo1.cloudexaminationplatform.Services.Impl;

import com.yuosef.demo1.cloudexaminationplatform.Daos.LabTemplateDao;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.LabDto;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.RequestTemplateDto;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.ResponseTemplateDto;

import com.yuosef.demo1.cloudexaminationplatform.Models.LabTemplate;
import com.yuosef.demo1.cloudexaminationplatform.Services.LabService;
import com.yuosef.demo1.cloudexaminationplatform.Services.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabServiceImpl implements LabService {

    private final LabTemplateDao labTemplateDao;
    private final UserService userService;
    public LabServiceImpl(LabTemplateDao labTemplateDao, UserService userService) {
        this.labTemplateDao = labTemplateDao;
        this.userService = userService;
    }

    @Override
    public Object createLab(LabDto labDto) {
        return null;
    }

    @Override
    public ResponseTemplateDto createTemplate(RequestTemplateDto requestTemplateDto) {
        LabTemplate saved= new LabTemplate(requestTemplateDto.amiName(),
                requestTemplateDto.amiId(),
                userService.getCurrentUser());
        LabTemplate labTemplate = labTemplateDao.save(saved);
        return new ResponseTemplateDto(labTemplate.getId(),
                labTemplate.getAmiName(),
                labTemplate.getAmiId(),
                labTemplate.getCreatedAt(),
                labTemplate.getCreatedBy()
        );
    }

    @Override
    public List<LabTemplate> getAlltemplatesByUserId() {
        return labTemplateDao.findByUserId(userService.getCurrentUser().getId());
    }


}
