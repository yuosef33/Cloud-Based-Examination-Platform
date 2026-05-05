package com.yuosef.cloudbasedlabexaminationplatform.controller;


import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.*;
import com.yuosef.cloudbasedlabexaminationplatform.models.Lab;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import com.yuosef.cloudbasedlabexaminationplatform.models.User;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabDao;
import com.yuosef.cloudbasedlabexaminationplatform.services.Impl.FileCollectionService;
import com.yuosef.cloudbasedlabexaminationplatform.services.Impl.S3Service;
import com.yuosef.cloudbasedlabexaminationplatform.services.Impl.TerraformService;
import com.yuosef.cloudbasedlabexaminationplatform.services.LabService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.SystemException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/lab")
@RequiredArgsConstructor
public class LabController {
    private final LabService labService;
    private final TerraformService terraformService;
    private final FileCollectionService fileCollectionService;
    private final S3Service s3Service;
    private final S3Client s3Client;
    private final LabDao labDao;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    @PostMapping("/CreateAmi")
    public ResponseEntity<ApiResponse> createLabTemplate(
            @RequestBody RequestTemplateDto requestTemplateDto) throws Exception {
        terraformService.createAmiFromVm(
                terraformService.checkVmExist(requestTemplateDto.VmId()),
                requestTemplateDto.amiName(),
                requestTemplateDto.osType()
        );
        return ResponseEntity.ok(new ApiResponse(true, "Template creation started", null));
    }
    @PostMapping("/addLabTemplate")
    public ResponseEntity<ApiResponse> addLabTemplate(@RequestBody RequestTemplateDto requestTemplateDto) {
        return ResponseEntity.ok(new ApiResponse(true," Template created ",labService.createTemplate(requestTemplateDto)));
    }
    @GetMapping("/GetAllAmi")
    public ResponseEntity<List<LabTemplate>> getAlltemplatesByUserId(){
        return ResponseEntity.ok(labService.getAlltemplatesByUserId());
    }
    @PostMapping("/Start/Base-template/{osType}")
    public ResponseEntity<TerraformOutput> startNewLapTemplate(
            @AuthenticationPrincipal User user,
            @PathVariable String osType) throws Exception {

        String baseAmi = "WINDOWS".equalsIgnoreCase(osType)
                ? TerraformService.Base_Windows_Ami
                : TerraformService.Base_Linux_Ami;

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(terraformService.createEc2WithSdk(user, baseAmi, null, osType));
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
    @GetMapping("/getMyLabs")
    public ResponseEntity<ApiResponse<?>> getAllMylabs(){
        return ResponseEntity.ok(new ApiResponse<>(true,"Get all labs",labService.getMyLabs()));
    }
    @PostMapping("/collectAll/{labId}")
    public ResponseEntity<ApiResponse<?>> collectAllFiles(@PathVariable Long labId){
        Lab lab = labDao.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        if (lab.getCollected()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Already collected", null));
        }

        fileCollectionService.collectAllStudentFiles(labId);
        return ResponseEntity.ok(new ApiResponse<>(true,
                "File collection started in background", null));
    }
    @GetMapping("/labs/{labId}/files")
    public ResponseEntity<ApiResponse<?>> getLabFiles(@PathVariable Long labId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lab files",
                s3Service.getLabFiles(labId)));
    }
    @GetMapping("/labs/{labId}/files/{studentId}/download")
    public void downloadStudentFiles(@PathVariable Long labId,
                                     @PathVariable String studentId,
                                     HttpServletResponse response) throws IOException {

        String prefix = "labs/" + labId + "/" + studentId + "/";

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .build()
        );

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"student-" + studentId + "-lab-" + labId + ".zip\"");

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (S3Object obj : listResponse.contents()) {
                String key = obj.key();
                String fileName = key.substring(key.lastIndexOf("/") + 1);

                ResponseBytes<GetObjectResponse> s3Object = s3Client.getObjectAsBytes(
                        GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build()
                );

                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.write(s3Object.asByteArray());
                zipOut.closeEntry();
            }
        }
    }

}
