package com.yuosef.cloudbasedlabexaminationplatform.controller;


import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.RequestTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.ResponseTemplateDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import com.yuosef.cloudbasedlabexaminationplatform.services.LabService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lab")
public class LabController {
    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }
    @PostMapping("/CreateAmi")
    public ResponseEntity<ResponseTemplateDto> createLabTemplate(@RequestBody RequestTemplateDto requestTemplateDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(labService.createTemplate(requestTemplateDto));
    }
    @GetMapping("/GetAllAmi")
    public ResponseEntity<List<LabTemplate>> getAlltemplatesByUserId(){
        return ResponseEntity.ok(labService.getAlltemplatesByUserId());
    }

}
