package com.code.delta.service4lambdauploader.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.code.delta.commondata.dto.JobPersistedDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class S3UploaderLambda implements RequestHandler<SQSEvent, Void> {

    private final S3Client s3Client = S3Client.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String bucketName = System.getenv("S3_BUCKET_NAME");

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        for (SQSEvent.SQSMessage msg : sqsEvent.getRecords()) {
            try {
                // 1. Deserialize the message body to our DTO
                JobPersistedDto persistedDto = objectMapper.readValue(msg.getBody(), JobPersistedDto.class);
                context.getLogger().log("Processing job: " + persistedDto.jobReqId());

                // 2. Generate JSON and ZIP in memory
                byte[] jsonBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(persistedDto);
                byte[] zipBytes = createZipInMemory(jsonBytes);

                // 3. Upload files to S3
                String jobFolderKey = persistedDto.jobReqId().toString();
                uploadToS3(jobFolderKey + "/output.json", jsonBytes, context);
                uploadToS3(jobFolderKey + "/output.zip", zipBytes, context);

                // Here, you would typically update the job status to 'COMPLETED'
                // This would require another call, perhaps to a database or another service.
                // For simplicity, we are omitting this step.

            } catch (IOException e) {
                context.getLogger().log("Error processing message: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private byte[] createZipInMemory(byte[] content) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {
            ZipEntry zipEntry = new ZipEntry("output.json");
            zos.putNextEntry(zipEntry);
            zos.write(content);
            zos.closeEntry();
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadToS3(String key, byte[] content, Context context) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
        context.getLogger().log("Successfully uploaded " + key + " to S3 bucket " + bucketName);
    }
}