package com.yuosef.cloudbasedlabexaminationplatform.services.Impl;

import com.yuosef.cloudbasedlabexaminationplatform.models.*;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.*;
import com.yuosef.cloudbasedlabexaminationplatform.models.Mappers.LabMapper;
import com.yuosef.cloudbasedlabexaminationplatform.models.Mappers.LabTemplateMapper;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabDao;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabTemplateDao;
import com.yuosef.cloudbasedlabexaminationplatform.repository.VmInstanceDao;
import com.yuosef.cloudbasedlabexaminationplatform.services.Impl.schedule.LabScheduler;
import com.yuosef.cloudbasedlabexaminationplatform.services.LabService;
import com.yuosef.cloudbasedlabexaminationplatform.services.UserService;
import jakarta.transaction.SystemException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LabServiceImpl implements LabService {

    private final LabDao labDao;
    private final LabTemplateDao labTemplateDao;
    private final UserService userService;
    private final LabScheduler labScheduler;
    private final TerraformService terraformService;
    private final VmInstanceDao vmInstanceDao;
    public LabServiceImpl(LabDao labDao, LabTemplateDao labTemplateDao, UserService userService, LabScheduler labScheduler, TerraformService terraformService, VmInstanceDao vmInstanceDao) {
        this.labDao = labDao;
        this.labTemplateDao = labTemplateDao;
        this.userService = userService;
        this.labScheduler = labScheduler;
        this.terraformService = terraformService;
        this.vmInstanceDao = vmInstanceDao;
    }

    @Override
    public Lab createLab(LabDto labDto) throws SystemException {
        LabTemplate template = labTemplateDao
                .findById(labDto.labTemplateId())
                .orElseThrow(() -> new SystemException("cant find template with that id"));

        if(template.getLabTemplateStatus()!=LabTemplateStatus.AVAILABLE){
            throw new SystemException("this "+template.getId()+" lab template is not available");
        }

        if(labDto.labStartTime().isBefore(LocalDateTime.now()))
            throw new SystemException("the date must be after the current date and time ");

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
    public List<LabDtoResponse> getalllabs(){
        List<Lab> labs= labDao.findAll();
        List<LabDtoResponse> labsDto=new ArrayList<>();
        for (Lab lab: labs ){
           labsDto.add(LabMapper.toDto(lab));
        }
        return labsDto;
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

    @Override
    public TerraformOutput createStudentVm(String labId) throws Exception {
        Lab lab = labDao.findById(Long.parseLong(labId))
                .orElseThrow(() -> new SystemException("failed to find this lab: " + labId));

        User currentUser = userService.getCurrentUser();

        // ← check if VM already exists before creating
        Optional<VmInstance> existingVm = vmInstanceDao
                .findByUserAndLabTemplateAndStatus(
                        currentUser,
                        lab.getLabTemplate(),
                        VmStatus.RUNNING
                );

        if (existingVm.isPresent()) {
            // VM already exists → return existing one
            return new TerraformOutput(
                    existingVm.get().getPublicIp(),
                    existingVm.get().getInstanceId()
            );
        }

        // no VM → create new one
        return terraformService.createEc2WithSdk(currentUser, lab.getLabTemplate().getAmiName());
    }
    @Override
    public TerraformOutput getStudentVm(String labId) throws SystemException {
        User currentUser = userService.getCurrentUser();
        Lab lab = labDao.findById(Long.parseLong(labId))
                .orElseThrow(() -> new SystemException("Lab not found"));

        // find running VM for this user and this lab's template
        VmInstance vm = vmInstanceDao
                .findByUserAndLabTemplateAndStatus(
                        currentUser,
                        lab.getLabTemplate(),
                        VmStatus.RUNNING
                )
                .orElse(null);

        if (vm == null) return null;

        return new TerraformOutput(vm.getPublicIp(), vm.getInstanceId());
    }
}
