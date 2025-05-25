package com.code.delta.service2persistence.listener;

import com.code.delta.commondata.dto.JobSubmissionDto;
import com.code.delta.service2persistence.service.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQJobListener {


    private static final Logger log = LoggerFactory.getLogger(RabbitMQJobListener.class);
   private final PersistenceService persistenceService;

    public RabbitMQJobListener(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.submission}")
    public void receiveJobSubmission(JobSubmissionDto submissionDto) {
        log.info("Received message from submission queue for job: {}", submissionDto.jobName());
        persistenceService.persistJob(submissionDto);
    }

}