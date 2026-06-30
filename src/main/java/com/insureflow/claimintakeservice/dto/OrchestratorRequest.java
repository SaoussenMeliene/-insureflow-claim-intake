package com.insureflow.claimintakeservice.dto;

import com.insureflow.claimintakeservice.model.ClaimType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrchestratorRequest {

    private Long claimId;
    private String reference;
    private String policyNumber;
    private ClaimType claimType;
    private String description;
    private String incidentLocation;
    private List<String> photoUrls;
    private String clientId;
    private String clientEmail;
}
