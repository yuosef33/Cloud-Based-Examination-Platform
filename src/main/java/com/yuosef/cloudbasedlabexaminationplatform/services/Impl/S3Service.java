package com.yuosef.cloudbasedlabexaminationplatform.services.Impl;

import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.FileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public Map<String, List<FileDto>> getLabFiles(Long labId) {
        String prefix = "labs/" + labId + "/";

        ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .build()
        );

        // group files by studentId
        // structure: labs/{labId}/{studentId}/filename
        Map<String, List<FileDto>> filesByStudent = new HashMap<>();

        for (S3Object obj : response.contents()) {
            String key = obj.key();
            String[] parts = key.split("/");
            if (parts.length < 4) continue;

            String studentId = parts[2];

            String studentPrefix = "labs/" + labId + "/" + studentId + "/";
            String relativePath = key.substring(studentPrefix.length());

            if (relativePath.isEmpty() || relativePath.endsWith("/")) continue;

            String fileName = parts[parts.length - 1];
            String downloadUrl = generatePresignedUrl(key, fileName);

            filesByStudent
                    .computeIfAbsent(studentId, k -> new ArrayList<>())
                    .add(new FileDto(relativePath, fileName, downloadUrl, obj.size()));
        }
        return filesByStudent;
    }

    private String generatePresignedUrl(String key, String fileName) {
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofHours(1))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .responseContentDisposition(
                                        "attachment; filename=\"" + fileName + "\""
                                )
                                .build())
                        .build()
        );
        return presigned.url().toString();
    }
}