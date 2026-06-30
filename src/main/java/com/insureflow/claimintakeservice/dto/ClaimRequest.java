package com.insureflow.claimintakeservice.dto;


import com.insureflow.claimintakeservice.model.ClaimType;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ClaimRequest {
    private String policyNumber;
    private LocalDate incidentDate;
    private ClaimType claimType;
    private String incidentLocation;
    private String description;
    private String clientId;
    private String clientEmail;
    private Double clientEstimatedCost; // estimation du client
    // ── Liste d'URLs des photos uploadées
    private List<String> photoUrls = new ArrayList<>();

    private String  vehicleBrand;
    private String  vehicleModel;
    private Integer vehicleYear;
    private String  vehicleCategory;
}