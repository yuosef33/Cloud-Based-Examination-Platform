package com.yuosef.cloudbasedlabexaminationplatform.services.Impl;

import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.LabDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.LabTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.RequestTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.ResponseTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Lab;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabStatus;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import com.yuosef.cloudbasedlabexaminationplatform.models.Mappers.LabTemplateMapper;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabDao;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabTemplateDao;
import com.yuosef.cloudbasedlabexaminationplatform.services.LabService;
import com.yuosef.cloudbasedlabexaminationplatform.services.UserService;
import jakarta.transaction.SystemException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LabServiceImpl implements LabService {

    private final LabDao labDao;
    private final LabTemplateDao labTemplateDao;
    private final UserService userService;
    public LabServiceImpl(LabDao labDao, LabTemplateDao labTemplateDao, UserService userService) {
        this.labDao = labDao;
        this.labTemplateDao = labTemplateDao;
        this.userService = userService;
    }

    @Override
    public Object createLab(LabDto labDto) throws SystemException {
        LabTemplate template = labTemplateDao
                .findById(Long.parseLong(labDto.labTemplate()))
                .orElseThrow(() -> new SystemException("cant find template with that id"));

        Lab lab = new Lab();
        lab.setLabName(labDto.labName());
        lab.setLabStartTime(LocalDateTime.now());
        lab.setLabDuration(Duration.ofMinutes(120));
        lab.setLabEndTime(LocalDateTime.now().plus(labDto.labDuration()));
        lab.setStatus(LabStatus.RUNNING);
        lab.setLabTemplate(template);

        return labDao.save(lab);
    }

    @Override
    public LabTemplateDto createTemplate(RequestTemplateDto requestTemplateDto) {
        LabTemplate saved= new LabTemplate(requestTemplateDto.amiName(),
                requestTemplateDto.VmId(),
                userService.getCurrentUser());
        LabTemplate labTemplate = labTemplateDao.save(saved);
        return LabTemplateMapper.toDto(labTemplate);
    }

    @Override
    public List<LabTemplate> getAlltemplatesByUserId() {
        return labTemplateDao.findAllByCreatedBy(userService.getCurrentUser());
    }


}
