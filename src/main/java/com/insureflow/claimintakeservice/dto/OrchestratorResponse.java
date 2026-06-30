package com.insureflow.claimintakeservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrchestratorResponse {

    private Long claimId;
    private boolean success;
    private String message;
    private Double confidenceScore;
    private String workflowStatus;
    private boolean requiresHuman;
}