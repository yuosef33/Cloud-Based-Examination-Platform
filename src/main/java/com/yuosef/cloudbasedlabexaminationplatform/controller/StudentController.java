package com.yuosef.cloudbasedlabexaminationplatform.controller;

import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.ApiResponse;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.LabDtoResponse;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.TerraformOutput;
import com.yuosef.cloudbasedlabexaminationplatform.services.LabService;
import jakarta.transaction.SystemException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/student")
public class StudentController {

    private final LabService labService;

    @GetMapping("/all-labs")
    public ResponseEntity<ApiResponse<List<LabDtoResponse>>> getAllLabs(){
        return ResponseEntity.ok(new ApiResponse<>(true,"all labs",labService.getalllabs()));
    }

    @PostMapping("/start-labTest")
    public ResponseEntity<ApiResponse<TerraformOutput>> startEc2ForStudent(@RequestParam String labId) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true,"all labs",labService.createStudentVm(labId)));
    }
    @GetMapping("/my-vm")
    public ResponseEntity<ApiResponse<TerraformOutput>> getMyVm(@RequestParam String labId) throws SystemException {
        return ResponseEntity.ok(new ApiResponse<>(true, "vm data", labService.getStudentVm(labId)));
    }
}
