package com.code.delta.commondata.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

// DTO sent from Service 2 to Service 3 after persistence
public record JobPersistedDto(
        UUID jobReqId,
        String jobName,
        Map<String, Object> payload
) implements Serializable {}