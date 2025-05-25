package com.code.delta.service1api.repository;

import com.code.delta.commondata.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    @Procedure(procedureName = "update_job_status")
    void updateJobStatus(
            @Param("p_job_req_id") UUID jobReqId,
            @Param("p_new_status") String newStatus,
            @Param("p_failure_reason") String failureReason
    );
}
