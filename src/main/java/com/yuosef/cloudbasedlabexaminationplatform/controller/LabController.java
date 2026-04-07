package com.yuosef.cloudbasedlabexaminationplatform.controller;


import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.*;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import com.yuosef.cloudbasedlabexaminationplatform.models.User;
import com.yuosef.cloudbasedlabexaminationplatform.services.Impl.TerraformService;
import com.yuosef.cloudbasedlabexaminationplatform.services.LabService;
import jakarta.transaction.SystemException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lab")
@RequiredArgsConstructor
public class LabController {
    private final LabService labService;
    private final TerraformService terraformService;

    @PostMapping("/CreateAmi")
    public ResponseEntity<ApiResponse> createLabTemplate(@RequestBody RequestTemplateDto requestTemplateDto) throws Exception {
        terraformService.createAmiFromVm(terraformService.checkVmExist(requestTemplateDto.VmId()),requestTemplateDto.amiName());
        return ResponseEntity.ok(new ApiResponse(true," Template creation started ",null));
    }
    @PostMapping("/addLabTemplate")
    public ResponseEntity<ApiResponse> addLabTemplate(@RequestBody RequestTemplateDto requestTemplateDto) {
        return ResponseEntity.ok(new ApiResponse(true," Template created ",labService.createTemplate(requestTemplateDto)));
    }
    @GetMapping("/GetAllAmi")
    public ResponseEntity<List<LabTemplate>> getAlltemplatesByUserId(){
        return ResponseEntity.ok(labService.getAlltemplatesByUserId());
    }
    @PostMapping("/Start/Base-template")
    public ResponseEntity<TerraformOutput> startNewLapTemplate(@AuthenticationPrincipal User user) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(terraformService.createEc2WithSdk(user,TerraformService.Base_Ami,null));
    }
    @DeleteMapping("/destroy-machine")
    public ResponseEntity<ApiResponse<?>> destroymachine(@RequestParam String id) throws Exception {
        terraformService.destroyVmWithSdk(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true,"successfully deleted", null));
    }
    @PostMapping("/create-lab")
    public ResponseEntity<ApiResponse<?>> createLab(@RequestBody LabDto labDto) throws SystemException {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true,"successfully created", labService.createLab(labDto)));
    }


}
