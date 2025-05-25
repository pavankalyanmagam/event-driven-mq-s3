package com.code.delta.service4lambdauploader.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.code.delta.service4lambdauploader.model.FileUploadRequest;
import com.code.delta.service4lambdauploader.model.FileUploadResponse;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Base64;
import java.util.UUID;

public class FileUploadLambda implements RequestHandler<FileUploadRequest, FileUploadResponse> {

    private final S3Client s3Client;
    private final SqsClient sqsClient;
    private final String s3BucketName;
    private final String sqsQueueUrl;

    public FileUploadLambda() {
        this.s3Client = S3Client.builder().build();
        this.sqsClient = SqsClient.builder().build();
        this.s3BucketName = System.getenv("UPLOAD_S3_BUCKET_NAME");
        this.sqsQueueUrl = System.getenv("SQS_QUEUE_URL");

        if (this.s3BucketName == null || this.s3BucketName.isEmpty()) {
            throw new IllegalStateException("Environment variable UPLOAD_S3_BUCKET_NAME is not set.");
        }
        if (this.sqsQueueUrl == null || this.sqsQueueUrl.isEmpty()) {
            throw new IllegalStateException("Environment variable SQS_QUEUE_URL is not set.");
        }
    }

    // Constructor for testing purposes
    public FileUploadLambda(S3Client s3Client, SqsClient sqsClient, String s3BucketName, String sqsQueueUrl) {
        this.s3Client = s3Client;
        this.sqsClient = sqsClient;
        this.s3BucketName = s3BucketName;
        this.sqsQueueUrl = sqsQueueUrl;
    }

    @Override
    public FileUploadResponse handleRequest(FileUploadRequest request, Context context) {
        context.getLogger().log("Received request for file: " + request.getFileName());
        context.getLogger().log("S3 Bucket Name: " + s3BucketName);
        context.getLogger().log("SQS Queue URL: " + sqsQueueUrl);

        String originalFileName = request.getFileName();
        String fileContent = request.getFileContent();

        if (originalFileName == null || originalFileName.isEmpty()) {
            context.getLogger().log("File name is missing.");
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        if (fileContent == null || fileContent.isEmpty()) {
            context.getLogger().log("File content is missing.");
            throw new IllegalArgumentException("File content cannot be null or empty.");
        }

        // Decode file content (assuming base64)
        byte[] decodedFileBytes;
        try {
            decodedFileBytes = Base64.getDecoder().decode(fileContent);
            context.getLogger().log("File content decoded successfully.");
        } catch (IllegalArgumentException e) {
            context.getLogger().log("Error decoding file content: " + e.getMessage());
            throw new IllegalArgumentException("Invalid Base64 encoded file content.", e);
        }

        // Generate a unique key for S3 object to avoid overwrites
        String s3ObjectKey = UUID.randomUUID().toString() + "-" + originalFileName;

        // Upload to S3
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(s3ObjectKey)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(decodedFileBytes));
            context.getLogger().log("File uploaded to S3 with key: " + s3ObjectKey);
        } catch (Exception e) {
            context.getLogger().log("Error uploading to S3: " + e.getMessage());
            // Potentially rethrow or handle as a failed response
            throw new RuntimeException("Error uploading file to S3: " + e.getMessage(), e);
        }

        // Send a message to SQS
        try {
            String sqsMessageBody = "{\"s3BucketName\":\"" + s3BucketName + "\",\"s3ObjectKey\":\"" + s3ObjectKey + "\"}";
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(sqsQueueUrl)
                    .messageBody(sqsMessageBody)
                    .build();
            sqsClient.sendMessage(sendMessageRequest);
            context.getLogger().log("Message sent to SQS: " + sqsMessageBody);
        } catch (Exception e) {
            context.getLogger().log("Error sending message to SQS: " + e.getMessage());
            // Potentially rethrow or handle as a failed response
            // Depending on requirements, may need to handle S3 upload rollback if SQS fails
            throw new RuntimeException("Error sending message to SQS: " + e.getMessage(), e);
        }

        String successMessage = "File " + originalFileName + " processed successfully. Uploaded to S3 as " + s3ObjectKey + " and notification sent to SQS.";
        return new FileUploadResponse(successMessage, s3ObjectKey);
    }
}
