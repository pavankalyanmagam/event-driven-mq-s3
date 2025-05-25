package com.code.delta.commondata.dto;

import java.io.Serializable;
import java.util.Map;

// DTO for the initial request from the client
public record JobSubmissionDto(
        String jobName,
        Map<String, Object> payload
) implements Serializable {}