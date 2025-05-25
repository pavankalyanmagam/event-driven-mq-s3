package com.code.delta.service3orchestrator.repository;


import com.code.delta.commondata.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Repository for the Job entity in Service 3.
 * Includes the specific method to call the status update stored procedure.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    @Transactional
    @Procedure(procedureName = "update_job_status")
    void updateJobStatus(
            @Param("p_job_req_id") UUID jobReqId,
            @Param("p_new_status") String newStatus,
            @Param("p_failure_reason") String failureReason
    );
}