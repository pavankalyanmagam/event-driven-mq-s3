package com.code.delta.service2persistence.service;


import com.code.delta.commondata.dto.JobPersistedDto;
import com.code.delta.commondata.dto.JobSubmissionDto;
import com.code.delta.commondata.entity.Job;
import com.code.delta.service2persistence.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PersistenceService {
    private static final Logger log = LoggerFactory.getLogger(PersistenceService.class);

    private final JobRepository jobRepository;
   private final JmsTemplate jmsTemplate;
    private final String persistenceQueue;
    //private final RabbitTemplate rabbitTemplate;
    // 1. Inject the persistence queue name
//    @Value("${rabbitmq.queue.persistence}")
//    private String persistenceQueueName;


    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.persistence}") // New property name
    private String persistenceTopicName;

    public PersistenceService(JobRepository jobRepository,
                              //RabbitTemplate rabbitTemplate
                              JmsTemplate jmsTemplate, @Value("${job.persistence.queue}") String persistenceQueue, KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.jobRepository = jobRepository;
        //this.rabbitTemplate = rabbitTemplate;
        this.jmsTemplate = jmsTemplate;
        this.persistenceQueue = persistenceQueue;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void persistJob(JobSubmissionDto submissionDto) {
        log.info("Persisting job: {}", submissionDto.jobName());
        try {
            // Create and save the Job entity
            Job job = new Job();
            job.setJobReqId(UUID.randomUUID());
            job.setStatus("SUBMITTED");
            job.setOriginalPayload(submissionDto.payload());
            jobRepository.save(job);

            log.info("Job persisted with ID: {}", job.getJobReqId());

            // Create the DTO for the next service
            JobPersistedDto persistedDto = new JobPersistedDto(
                    job.getJobReqId(),
                    submissionDto.jobName(),
                    submissionDto.payload()
            );

            // Send to the next queue
           // jmsTemplate.convertAndSend(persistenceQueue, persistedDto);
            //rabbitTemplate.convertAndSend(persistenceQueueName, persistedDto);

            // Send to Kafka: topic, key, payload
            kafkaTemplate.send(persistenceTopicName, job.getJobReqId().toString(), persistedDto);


        } catch (Exception e) {
            log.error("Failed to persist job '{}'", submissionDto.jobName(), e);
            // Error handling: The message will be re-queued by default or sent to a DLQ if configured.
        }
    }
}