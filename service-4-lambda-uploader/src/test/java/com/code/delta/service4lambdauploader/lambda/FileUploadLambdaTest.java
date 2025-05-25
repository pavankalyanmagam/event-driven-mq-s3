package com.code.delta.service4lambdauploader.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.code.delta.service4lambdauploader.model.FileUploadRequest;
import com.code.delta.service4lambdauploader.model.FileUploadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileUploadLambdaTest {

    private FileUploadLambda fileUploadLambda;

    @Mock
    private S3Client mockS3Client;

    @Mock
    private SqsClient mockSqsClient;

    @Mock
    private Context mockContext;

    @Mock
    private LambdaLogger mockLambdaLogger;

    private static final String TEST_BUCKET_NAME = "test-bucket";
    private static final String TEST_SQS_QUEUE_URL = "test-queue-url";
    private static final String TEST_FILE_NAME = "test.txt";
    private static final String TEST_FILE_CONTENT_PLAIN = "Hello World!";
    private static final String TEST_FILE_CONTENT_BASE64 = Base64.getEncoder().encodeToString(TEST_FILE_CONTENT_PLAIN.getBytes());

    @BeforeEach
    void setUp() {
        // Initialize FileUploadLambda with mock clients and test environment variables
        fileUploadLambda = new FileUploadLambda(mockS3Client, mockSqsClient, TEST_BUCKET_NAME, TEST_SQS_QUEUE_URL);

        // Mock Context and Logger
        when(mockContext.getLogger()).thenReturn(mockLambdaLogger);
        // You can mock specific logger calls if needed, e.g., doNothing().when(mockLambdaLogger).log(anyString());
    }

    @Test
    void testHandleRequest_Success() {
        FileUploadRequest request = new FileUploadRequest(TEST_FILE_NAME, TEST_FILE_CONTENT_BASE64);

        // Mock S3 putObject
        PutObjectResponse putObjectResponse = PutObjectResponse.builder().eTag("test-etag").build();
        when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(putObjectResponse);

        // Mock SQS sendMessage
        SendMessageResponse sendMessageResponse = SendMessageResponse.builder().messageId("test-message-id").build();
        when(mockSqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(sendMessageResponse);

        // Call the handler
        FileUploadResponse response = fileUploadLambda.handleRequest(request, mockContext);

        // Assertions
        assertNotNull(response);
        assertTrue(response.getMessage().contains("processed successfully"));
        assertNotNull(response.getS3ObjectKey());
        assertTrue(response.getS3ObjectKey().endsWith("-" + TEST_FILE_NAME));

        // Verify S3 interaction
        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(mockS3Client, times(1)).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());
        assertEquals(TEST_BUCKET_NAME, putObjectRequestCaptor.getValue().bucket());
        assertTrue(putObjectRequestCaptor.getValue().key().endsWith("-" + TEST_FILE_NAME));
        // Add more assertions for RequestBody if necessary, e.g., content length or stream content comparison

        // Verify SQS interaction
        ArgumentCaptor<SendMessageRequest> sendMessageRequestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(mockSqsClient, times(1)).sendMessage(sendMessageRequestCaptor.capture());
        assertEquals(TEST_SQS_QUEUE_URL, sendMessageRequestCaptor.getValue().queueUrl());
        assertTrue(sendMessageRequestCaptor.getValue().messageBody().contains("\"s3BucketName\":\"" + TEST_BUCKET_NAME + "\""));
        assertTrue(sendMessageRequestCaptor.getValue().messageBody().contains("\"s3ObjectKey\":\"" + response.getS3ObjectKey() + "\""));

        verify(mockLambdaLogger, atLeastOnce()).log(anyString());
    }

    @Test
    void testHandleRequest_S3UploadFails() {
        FileUploadRequest request = new FileUploadRequest(TEST_FILE_NAME, TEST_FILE_CONTENT_BASE64);

        // Mock S3 putObject to throw an exception
        when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 upload error").build());

        // Call the handler and assert exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileUploadLambda.handleRequest(request, mockContext);
        });

        assertTrue(exception.getMessage().contains("Error uploading file to S3"));

        // Verify S3 interaction
        verify(mockS3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Verify SQS interaction (should not be called)
        verify(mockSqsClient, never()).sendMessage(any(SendMessageRequest.class));
        verify(mockLambdaLogger, atLeastOnce()).log(anyString());
    }

    @Test
    void testHandleRequest_SQSSendMessageFails() {
        FileUploadRequest request = new FileUploadRequest(TEST_FILE_NAME, TEST_FILE_CONTENT_BASE64);

        // Mock S3 putObject for success
        PutObjectResponse putObjectResponse = PutObjectResponse.builder().eTag("test-etag").build();
        when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(putObjectResponse);

        // Mock SQS sendMessage to throw an exception
        when(mockSqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(SqsException.builder().message("SQS send error").build());

        // Call the handler and assert exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileUploadLambda.handleRequest(request, mockContext);
        });

        assertTrue(exception.getMessage().contains("Error sending message to SQS"));

        // Verify S3 interaction
        verify(mockS3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Verify SQS interaction
        verify(mockSqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
        verify(mockLambdaLogger, atLeastOnce()).log(anyString());
    }

    @Test
    void testHandleRequest_InvalidBase64() {
        FileUploadRequest request = new FileUploadRequest(TEST_FILE_NAME, "This is not valid Base64!");

        // Call the handler and assert exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadLambda.handleRequest(request, mockContext);
        });

        assertTrue(exception.getMessage().contains("Invalid Base64 encoded file content"));

        // Verify no interactions with S3 or SQS
        verify(mockS3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(mockSqsClient, never()).sendMessage(any(SendMessageRequest.class));
        verify(mockLambdaLogger, atLeastOnce()).log(anyString());
    }

    @Test
    void testHandleRequest_MissingFileName() {
        FileUploadRequest request = new FileUploadRequest(null, TEST_FILE_CONTENT_BASE64);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadLambda.handleRequest(request, mockContext);
        });
        assertEquals("File name cannot be null or empty.", exception.getMessage());
        verify(mockLambdaLogger, atLeastOnce()).log("File name is missing.");
    }

    @Test
    void testHandleRequest_EmptyFileName() {
        FileUploadRequest request = new FileUploadRequest("", TEST_FILE_CONTENT_BASE64);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadLambda.handleRequest(request, mockContext);
        });
        assertEquals("File name cannot be null or empty.", exception.getMessage());
        verify(mockLambdaLogger, atLeastOnce()).log("File name is missing.");
    }

    @Test
    void testHandleRequest_MissingFileContent() {
        FileUploadRequest request = new FileUploadRequest(TEST_FILE_NAME, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadLambda.handleRequest(request, mockContext);
        });
        assertEquals("File content cannot be null or empty.", exception.getMessage());
        verify(mockLambdaLogger, atLeastOnce()).log("File content is missing.");
    }

    @Test
    void testHandleRequest_EmptyFileContent() {
        FileUploadRequest request = new FileUploadRequest(TEST_FILE_NAME, "");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadLambda.handleRequest(request, mockContext);
        });
        assertEquals("File content cannot be null or empty.", exception.getMessage());
        verify(mockLambdaLogger, atLeastOnce()).log("File content is missing.");
    }
}
