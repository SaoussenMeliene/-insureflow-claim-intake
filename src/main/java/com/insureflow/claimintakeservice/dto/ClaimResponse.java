package com.insureflow.claimintakeservice.dto;

import com.insureflow.claimintakeservice.model.ClaimStatus;
import com.insureflow.claimintakeservice.model.ClaimType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


    @Data
    @Builder
    public class ClaimResponse {
        private Long id;
        private String reference;
        private String policyNumber;
        private ClaimType claimType;
        private ClaimStatus status;
        private String clientEmail;
        private Double confidenceScore;
        private List<String> photoUrls;
        private Double  clientEstimatedCost;// ← List<String>
        private LocalDateTime createdAt;
        private String message;
        private String  vehicleBrand;
        private String  vehicleModel;
        private Integer vehicleYear;
        private String  vehicleCategory;

        private String        description;
        private String        incidentLocation;
        private LocalDate incidentDate;
        private String        clientId;
}
