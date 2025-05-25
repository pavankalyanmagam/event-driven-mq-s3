package com.code.delta.service2persistence.listener;


import com.code.delta.commondata.dto.JobSubmissionDto;
import com.code.delta.service2persistence.service.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobListener {
    private static final Logger log = LoggerFactory.getLogger(JobListener.class);
    private final PersistenceService persistenceService;


    public JobListener(PersistenceService persistenceService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.persistenceService = persistenceService;
    }

//    @JmsListener(destination = "${job.submission.queue}")
//    public void receiveJobSubmission(JobSubmissionDto submissionDto) {
//        log.info("Received message from submission queue for job: {}", submissionDto.jobName());
//        persistenceService.persistJob(submissionDto);
//    }

    @KafkaListener(topics = "${kafka.topic.submission}", groupId = "persistence-group")
    public void receiveJobSubmission(JobSubmissionDto submissionDto) {
        log.info("Received message from submission queue for job: {}", submissionDto.jobName());
        persistenceService.persistJob(submissionDto);
    }


}