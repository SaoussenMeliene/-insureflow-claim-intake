package com.insureflow.claimintakeservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    @Column(nullable = false)
    private String policyNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private String clientEmail;

    @Column(nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false)
    private String incidentLocation;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private Double confidenceScore;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    // ── AJOUTER CES 5 CHAMPS ─────────────────
    @Column(nullable = false)
    private Double clientEstimatedCost;  // ← OBLIGATOIRE

    private String  vehicleBrand;        // ← OPTIONNEL
    private String  vehicleModel;        // ← OPTIONNEL
    private Integer vehicleYear;         // ← OPTIONNEL
    private String  vehicleCategory;     // ← OPTIONNEL
    // ─────────────────────────────────────────

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status    = ClaimStatus.SUBMITTED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}