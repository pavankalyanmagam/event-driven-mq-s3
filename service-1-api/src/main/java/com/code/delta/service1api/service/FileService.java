package com.code.delta.service1api.service;


import com.code.delta.commondata.dto.JobPersistedDto;
import com.code.delta.service1api.repository.JobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FileService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
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
}