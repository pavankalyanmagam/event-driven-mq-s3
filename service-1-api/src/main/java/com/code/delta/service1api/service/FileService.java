package com.code.delta.service1api.service;


import com.code.delta.commondata.dto.JobPersistedDto;
import com.code.delta.service1api.repository.JobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.s3.S3Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;
    private final S3Template s3Template;
    public FileService(JobRepository jobRepository, ObjectMapper objectMapper, S3Template s3Template) {
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
        this.s3Template = s3Template;
    }

    public void generateFiles(JobPersistedDto dto) {
        Path outputDir = Paths.get("output", dto.jobReqId().toString());
        try {
            log.info("Starting file generation for job: {}", dto.jobReqId());
            Files.createDirectories(outputDir);

            // Create output.json
            Path jsonPath = outputDir.resolve("output.json");
            Files.write(jsonPath, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(dto));

            // Create output.zip
            Path zipPath = outputDir.resolve("output.zip");
            try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                File fileToZip = jsonPath.toFile();
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zos.putNextEntry(zipEntry);
                Files.copy(jsonPath, zos);
                zos.closeEntry();
            }

            log.info("Successfully generated files for job: {}", dto.jobReqId());
            jobRepository.updateJobStatus(dto.jobReqId(), "COMPLETED", null);

        } catch (IOException e) {
            log.error("Failed to generate files for job: {}", dto.jobReqId(), e);
            jobRepository.updateJobStatus(dto.jobReqId(), "FAILED", "Error during file generation: " + e.getMessage());
        }
    }
    public void generateAndUploadFiles(JobPersistedDto dto) {
        String jobFolderKey = dto.jobReqId().toString(); // Use the job ID as a "folder" in S3

        try {
            log.info("Starting file generation and upload for job: {}", dto.jobReqId());

            // 1. Create JSON content in memory
            byte[] jsonBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(dto);
            String jsonFileKey = jobFolderKey + "/output.json";

            // 2. Upload JSON file to S3
            try (InputStream jsonInputStream = new ByteArrayInputStream(jsonBytes)) {
                s3Template.upload(bucketName, jsonFileKey, jsonInputStream);
                log.info("Successfully uploaded {} to S3 bucket {}", jsonFileKey, bucketName);
            }

            // 3. Create ZIP file in memory
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {
                ZipEntry zipEntry = new ZipEntry("output.json");
                zos.putNextEntry(zipEntry);
                zos.write(jsonBytes);
                zos.closeEntry();
            }
            byte[] zipBytes = byteArrayOutputStream.toByteArray();
            String zipFileKey = jobFolderKey + "/output.zip";

            // 4. Upload ZIP file to S3
            try (InputStream zipInputStream = new ByteArrayInputStream(zipBytes)) {
                s3Template.upload(bucketName, zipFileKey, zipInputStream);
                log.info("Successfully uploaded {} to S3 bucket {}", zipFileKey, bucketName);
            }

            // 5. Update the job status to COMPLETED
            jobRepository.updateJobStatus(dto.jobReqId(), "COMPLETED", "Files uploaded to S3 bucket: " + bucketName + "/" + jobFolderKey);

        } catch (IOException e) {
            log.error("Failed to generate and upload files for job: {}", dto.jobReqId(), e);
            jobRepository.updateJobStatus(dto.jobReqId(), "FAILED", "Error during S3 upload: " + e.getMessage());
        }
    }
}