package com.yuosef.demo1.cloudexaminationplatform.Controller;

import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.RequestTemplateDto;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.ResponseTemplateDto;
import com.yuosef.demo1.cloudexaminationplatform.Models.LabTemplate;
import com.yuosef.demo1.cloudexaminationplatform.Services.LabService;
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
