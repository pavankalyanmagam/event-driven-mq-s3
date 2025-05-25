package com.code.delta.service1api.controller;

import com.code.delta.commondata.dto.JobPersistedDto;
import com.code.delta.commondata.dto.JobSubmissionDto;
import com.code.delta.service1api.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
//import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private static final Logger log = LoggerFactory.getLogger(JobController.class);
    // 1. Inject the queue name from application.properties
//    @Value("${rabbitmq.queue.submission}")
//    private String submissionQueueName;
    private final JmsTemplate jmsTemplate;
    private final FileService fileService;
   private final String submissionQueue;
   // private final RabbitTemplate rabbitTemplate;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.submission}") // New property name
    private String submissionTopicName;


    public JobController(
            //JmsTemplate jmsTemplate,
            FileService fileService,
            @Value("${job.submission.queue}") String submissionQueue, JmsTemplate jmsTemplate, KafkaTemplate<String, Object> kafkaTemplate
            //RabbitTemplate rabbitTemplate
    ) {
        //this.jmsTemplate = jmsTemplate;
        this.fileService = fileService;
        this.jmsTemplate = jmsTemplate;
        this.submissionQueue = submissionQueue;
        //this.rabbitTemplate = rabbitTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<String> submitJob(@RequestBody JobSubmissionDto submissionDto) {
        try {
            log.info("Received job submission: {}", submissionDto.jobName());
           // jmsTemplate.convertAndSend(submissionQueue, submissionDto);
            // 3. Use the injected field here
            //rabbitTemplate.convertAndSend(submissionQueueName, submissionDto);

            // Send to Kafka: topic, key (can be null or a specific ID), payload
            kafkaTemplate.send(submissionTopicName, submissionDto);

            return ResponseEntity.accepted().body("{\"message\": \"Job accepted for processing.\"}");
        } catch (Exception e) {
            log.error("Failed to send job to queue", e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Could not queue job for processing.\"}");
        }
    }

    @PostMapping("/process-final")
    public ResponseEntity<Void> processFinalJobStep(@RequestBody JobPersistedDto persistedDto) {
        log.info("Received request to process final step for job: {}", persistedDto.jobReqId());
        // Run async to not block the HTTP thread from Service 3
        CompletableFuture.runAsync(() -> fileService.generateFiles(persistedDto));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload-to-s3")
    public ResponseEntity<Void> uploadToS3(@RequestBody JobPersistedDto persistedDto) {
        log.info("Received request to process final step for job: {}", persistedDto.jobReqId());
        // Run async to not block the HTTP thread from Service 3
        CompletableFuture.runAsync(() ->
                fileService.generateAndUploadFiles(persistedDto));
        return ResponseEntity.ok().build();
    }
}