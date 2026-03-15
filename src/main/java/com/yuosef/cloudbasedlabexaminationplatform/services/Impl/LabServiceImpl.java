package com.yuosef.cloudbasedlabexaminationplatform.services.Impl;

import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.LabDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.LabTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.RequestTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.ResponseTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Lab;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabStatus;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplateStatus;
import com.yuosef.cloudbasedlabexaminationplatform.models.Mappers.LabTemplateMapper;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabDao;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabTemplateDao;
import com.yuosef.cloudbasedlabexaminationplatform.services.Impl.schedule.LabScheduler;
import com.yuosef.cloudbasedlabexaminationplatform.services.LabService;
import com.yuosef.cloudbasedlabexaminationplatform.services.UserService;
import jakarta.transaction.SystemException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class LabServiceImpl implements LabService {

    private final LabDao labDao;
    private final LabTemplateDao labTemplateDao;
    private final UserService userService;
    private final LabScheduler labScheduler;
    public LabServiceImpl(LabDao labDao, LabTemplateDao labTemplateDao, UserService userService, LabScheduler labScheduler) {
        this.labDao = labDao;
        this.labTemplateDao = labTemplateDao;
        this.userService = userService;
        this.labScheduler = labScheduler;
    }

    @Override
    public Lab createLab(LabDto labDto) throws SystemException {
        LabTemplate template = labTemplateDao
                .findById(labDto.labTemplateId())
                .orElseThrow(() -> new SystemException("cant find template with that id"));

        if(template.getLabTemplateStatus()!=LabTemplateStatus.AVAILABLE){
            throw new SystemException("this "+template.getId()+" lab template is not available");
        }

        Lab lab = new Lab();
        lab.setLabName(labDto.labName());
        lab.setLabDescription(labDto.labDescription());
        lab.setLabInstructions(labDto.labInstructions());
        lab.setLabStartTime(labDto.labStartTime());
        lab.setLabDuration(Duration.ofMinutes(labDto.labDuration()));
        lab.setLabEndTime(labDto.labStartTime().plus(Duration.ofMinutes(labDto.labDuration())));
        lab.setStatus(LabStatus.CREATED);
        lab.setLabTemplate(template);

        labScheduler.scheduleLab(lab);
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
