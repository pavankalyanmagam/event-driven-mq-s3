package com.code.delta.commondata.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "jobs_data")
public class Job {

    @Id
    @Column(name = "job_req_id")
    private UUID jobReqId;

    @Column(name = "status", nullable = false)
    private String status;

    @Type(JsonBinaryType.class)
    @Column(name = "original_payload", columnDefinition = "jsonb")
    private Map<String, Object> originalPayload;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private OffsetDateTime lastUpdatedAt;

    @PrePersist
    public void onPrePersist() {
        createdAt = OffsetDateTime.now();
        lastUpdatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        lastUpdatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getJobReqId() { return jobReqId; }
    public void setJobReqId(UUID jobReqId) { this.jobReqId = jobReqId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, Object> getOriginalPayload() { return originalPayload; }
    public void setOriginalPayload(Map<String, Object> originalPayload) { this.originalPayload = originalPayload; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(OffsetDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}