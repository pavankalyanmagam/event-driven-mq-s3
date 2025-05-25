package com.code.delta.service3orchestrator.service;


import com.code.delta.commondata.dto.JobPersistedDto;
import com.code.delta.service3orchestrator.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationService.class);

    private final JobRepository jobRepository;
    private final WebClient webClient;
    private final String service1ApiUrl;

    public OrchestrationService(JobRepository jobRepository, WebClient.Builder webClientBuilder, @Value("${service1.api.url}") String service1ApiUrl) {
        this.jobRepository = jobRepository;
        this.webClient = webClientBuilder.build();
        this.service1ApiUrl = service1ApiUrl;
    }

    public void triggerFinalProcessing(JobPersistedDto persistedDto) {
        log.info("Orchestrating final step for job: {}", persistedDto.jobReqId());

        try {
            // 1. Update status to PROCESSING
            jobRepository.updateJobStatus(persistedDto.jobReqId(), "PROCESSING", null);
            log.info("Status updated to PROCESSING for job: {}", persistedDto.jobReqId());

            // 2. Make API call to Service 1
            webClient.post()
                    .uri(service1ApiUrl)
                    .body(Mono.just(persistedDto), JobPersistedDto.class)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(response ->
                            log.info("Successfully triggered final processing for job: {}. Status: {}", persistedDto.jobReqId(), response.getStatusCode())
                    )
                    .doOnError(error -> {
                        log.error("Failed to trigger final processing for job: {}", persistedDto.jobReqId(), error);
                        jobRepository.updateJobStatus(persistedDto.jobReqId(), "FAILED", "API call to file generator failed: " + error.getMessage());
                    })
                    .subscribe(); // Subscribe to execute the call

        } catch (Exception e) {
            log.error("A critical error occurred during orchestration for job: {}", persistedDto.jobReqId(), e);
            jobRepository.updateJobStatus(persistedDto.jobReqId(), "FAILED", "Orchestration service critical failure: " + e.getMessage());
        }
    }
}