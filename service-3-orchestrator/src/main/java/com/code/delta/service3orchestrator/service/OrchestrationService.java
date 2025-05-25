package com.code.delta.service3orchestrator.service;


import com.code.delta.commondata.dto.JobPersistedDto;
import com.code.delta.service3orchestrator.repository.JobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

@Service
public class OrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationService.class);

    private final JobRepository jobRepository;
    private final WebClient webClient;
    private final String service1ApiUrl;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public OrchestrationService(JobRepository jobRepository, WebClient.Builder webClientBuilder, @Value("${service1.api.url}") String service1ApiUrl, SqsClient sqsClient, ObjectMapper objectMapper, @Value("${aws.sqs.queue.url}") String queueUrl) {
        this.jobRepository = jobRepository;
        this.webClient = webClientBuilder.build();
        this.service1ApiUrl = service1ApiUrl;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    // This to send REST call to Service 1
//    public void triggerFinalProcessing(JobPersistedDto persistedDto) {
//        log.info("Orchestrating final step for job: {}", persistedDto.jobReqId());
//
//        try {
//            // 1. Update status to PROCESSING
//            jobRepository.updateJobStatus(persistedDto.jobReqId(), "PROCESSING", null);
//            log.info("Status updated to PROCESSING for job: {}", persistedDto.jobReqId());
//
//            // 2. Make API call to Service 1
//            webClient.post()
//                    .uri(service1ApiUrl)
//                    .body(Mono.just(persistedDto), JobPersistedDto.class)
//                    .retrieve()
//                    .toBodilessEntity()
//                    .doOnSuccess(response ->
//                            log.info("Successfully triggered final processing for job: {}. Status: {}", persistedDto.jobReqId(), response.getStatusCode())
//                    )
//                    .doOnError(error -> {
//                        log.error("Failed to trigger final processing for job: {}", persistedDto.jobReqId(), error);
//                        jobRepository.updateJobStatus(persistedDto.jobReqId(), "FAILED", "API call to file generator failed: " + error.getMessage());
//                    })
//                    .subscribe(); // Subscribe to execute the call
//
//        } catch (Exception e) {
//            log.error("A critical error occurred during orchestration for job: {}", persistedDto.jobReqId(), e);
//            jobRepository.updateJobStatus(persistedDto.jobReqId(), "FAILED", "Orchestration service critical failure: " + e.getMessage());
//        }
//    }


    // This is to send SQS Message to trigger Lambda.
    public void triggerFinalProcessing(JobPersistedDto persistedDto) {
        log.info("Orchestrating final step for job: {}", persistedDto.jobReqId());

        try {
            // 1. Update status to PROCESSING
            jobRepository.updateJobStatus(persistedDto.jobReqId(), "PROCESSING", null);
            log.info("Status updated to PROCESSING for job: {}", persistedDto.jobReqId());

            // 2. Serialize DTO to JSON
            String messageBody = objectMapper.writeValueAsString(persistedDto);

            // 3. Send message to SQS
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl) // <-- CORRECTED from queryUrl to queueUrl
                    .messageBody(messageBody)
                    // For FIFO queues, a MessageGroupId is required.
                    .messageGroupId(persistedDto.jobReqId().toString())
                    // For FIFO queues, a deduplication ID is required to prevent processing duplicates.
                    .messageDeduplicationId(UUID.randomUUID().toString())
                    .build();

            sqsClient.sendMessage(sendMessageRequest);
            log.info("Successfully sent job {} to SQS queue.", persistedDto.jobReqId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize DTO for job: {}", persistedDto.jobReqId(), e);
            jobRepository.updateJobStatus(persistedDto.jobReqId(), "FAILED", "Failed to serialize DTO for SQS.");
        } catch (Exception e) {
            log.error("A critical error occurred during orchestration for job: {}", persistedDto.jobReqId(), e);
            jobRepository.updateJobStatus(persistedDto.jobReqId(), "FAILED", "Orchestration service critical failure: " + e.getMessage());
        }
    }
}