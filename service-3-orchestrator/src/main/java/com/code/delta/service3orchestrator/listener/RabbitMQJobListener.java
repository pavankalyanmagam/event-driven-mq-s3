//package com.code.delta.service3orchestrator.listener;
//
//import com.code.delta.commondata.dto.JobPersistedDto;
//import com.code.delta.service3orchestrator.service.OrchestrationService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RabbitMQJobListener {
//
//
//    private static final Logger log = LoggerFactory.getLogger(RabbitMQJobListener.class);
//    private final OrchestrationService orchestrationService;
//
//    public RabbitMQJobListener(OrchestrationService orchestrationService) {
//        this.orchestrationService = orchestrationService;
//    }
//
//    /**
//     * This method listens to the 'job-persistence-notifications' queue.
//     * When a message arrives, this code will execute.
//     * @param persistedDto The message payload, automatically converted from JSON to our DTO.
//     */
//    @RabbitListener(queues = "${rabbitmq.queue.persistence}")
//    public void receivePersistedJob(JobPersistedDto persistedDto) {
//        log.info("Received persisted job notification with ID: {}", persistedDto.jobReqId());
//        orchestrationService.triggerFinalProcessing(persistedDto);
//    }
//
//}