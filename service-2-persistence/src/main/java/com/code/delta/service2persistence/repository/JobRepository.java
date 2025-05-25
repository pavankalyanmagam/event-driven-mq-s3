package com.code.delta.service2persistence.repository;


import com.code.delta.commondata.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for basic CRUD operations on the Job entity.
 * This is used by Service 2 to save the initial job record.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    // JpaRepository provides all the necessary methods like save(), findById(), etc.
    // No additional methods are needed for this service.
}