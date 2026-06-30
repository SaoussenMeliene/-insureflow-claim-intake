package com.insureflow.claimintakeservice.service;


import com.insureflow.claimintakeservice.dto.ClaimRequest;
import com.insureflow.claimintakeservice.dto.ClaimResponse;
import com.insureflow.claimintakeservice.model.Claim;
import com.insureflow.claimintakeservice.model.ClaimStatus;
import com.insureflow.claimintakeservice.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {

    private final ClaimRepository claimRepository;

    // ════════════════════════════════════════════
    // SOUMETTRE UN NOUVEAU SINISTRE
    // ════════════════════════════════════════════
    public ClaimResponse submitClaim(ClaimRequest request) {
        log.info("Nouveau sinistre reçu pour client : {}",
                request.getClientId());

        // Générer une référence unique
        String reference = generateReference();

        // Construire l'entité Claim
        Claim claim = Claim.builder()
                .reference(reference)
                .policyNumber(request.getPolicyNumber())
                .claimType(request.getClaimType())
                .clientId(request.getClientId())
                .clientEmail(request.getClientEmail())
                .incidentDate(request.getIncidentDate())
                .incidentLocation(request.getIncidentLocation())
                .description(request.getDescription())
                .status(ClaimStatus.SUBMITTED)
                .photoUrls(request.getPhotoUrls())
                // ← AJOUTER
                .clientEstimatedCost(request.getClientEstimatedCost())
                .vehicleBrand(request.getVehicleBrand())
                .vehicleModel(request.getVehicleModel())
                .vehicleYear(request.getVehicleYear())
                .vehicleCategory(request.getVehicleCategory())
                .build();


        // Sauvegarder en base de données
        Claim saved = claimRepository.save(claim);
        log.info("Sinistre sauvegardé avec référence : {}",
                reference);

        return mapToResponse(saved,
                "Sinistre soumis avec succès. " +
                        "Référence : " + reference);
    }

    // ════════════════════════════════════════════
    // RÉCUPÉRER UN SINISTRE PAR ID
    // ════════════════════════════════════════════
    public ClaimResponse getClaimById(Long id) {
        log.info("Recherche sinistre ID : {}", id);

        Claim claim = claimRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Sinistre non trouvé avec ID : " + id));

        return mapToResponse(claim, null);
    }

    // ════════════════════════════════════════════
    // RÉCUPÉRER UN SINISTRE PAR RÉFÉRENCE
    // ════════════════════════════════════════════
    public ClaimResponse getClaimByReference(String reference) {
        log.info("Recherche sinistre référence : {}", reference);

        Claim claim = claimRepository
                .findByReference(reference)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Sinistre non trouvé : " + reference));

        return mapToResponse(claim, null);
    }

    // ════════════════════════════════════════════
    // RÉCUPÉRER TOUS LES SINISTRES D'UN CLIENT
    // ════════════════════════════════════════════
    public List<ClaimResponse> getClaimsByClient(
            String clientId) {
        log.info("Recherche sinistres client : {}", clientId);

        return claimRepository
                .findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(c -> mapToResponse(c, null))
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════
    // RÉCUPÉRER TOUS LES SINISTRES
    // ════════════════════════════════════════════
    public List<ClaimResponse> getAllClaims() {
        log.info("Récupération de tous les sinistres");

        return claimRepository.findAll()
                .stream()
                .map(c -> mapToResponse(c, null))
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════
    // RÉCUPÉRER SINISTRES PAR STATUT
    // ════════════════════════════════════════════
    public List<ClaimResponse> getClaimsByStatus(
            ClaimStatus status) {
        log.info("Recherche sinistres avec statut : {}", status);

        return claimRepository.findByStatus(status)
                .stream()
                .map(c -> mapToResponse(c, null))
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════
    // METTRE À JOUR LE STATUT D'UN SINISTRE
    // ════════════════════════════════════════════
    public ClaimResponse updateClaimStatus(
            Long id, ClaimStatus newStatus) {
        log.info("Mise à jour statut sinistre {} → {}",
                id, newStatus);

        Claim claim = claimRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Sinistre non trouvé : " + id));

        claim.setStatus(newStatus);
        Claim updated = claimRepository.save(claim);

        return mapToResponse(updated,
                "Statut mis à jour : " + newStatus);
    }

    // ════════════════════════════════════════════
    // METTRE À JOUR LE SCORE DE CONFIANCE
    // ════════════════════════════════════════════
    public ClaimResponse updateConfidenceScore(
            Long id, Double score) {
        log.info("Mise à jour score confiance sinistre {} → {}",
                id, score);

        Claim claim = claimRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Sinistre non trouvé : " + id));

        claim.setConfidenceScore(score);

        // Si score < 0.75 → Human-in-the-Loop requis
        if (score < 0.75) {
            claim.setStatus(ClaimStatus.HUMAN_REQUIRED);
            log.warn("Score {} < 0.75 → " +
                            "Human-in-the-Loop requis pour sinistre {}",
                    score, id);
        }

        Claim updated = claimRepository.save(claim);
        return mapToResponse(updated, null);
    }

    // ════════════════════════════════════════════
    // SUPPRIMER UN SINISTRE
    // ════════════════════════════════════════════
    public void deleteClaim(Long id) {
        log.info("Suppression sinistre ID : {}", id);

        if (!claimRepository.existsById(id)) {
            throw new RuntimeException(
                    "Sinistre non trouvé : " + id);
        }

        claimRepository.deleteById(id);
        log.info("Sinistre {} supprimé avec succès", id);
    }

    // ════════════════════════════════════════════
    // MÉTHODES PRIVÉES — UTILITAIRES
    // ════════════════════════════════════════════

    // ── Générer une référence unique CLM-XXXXXXXX
    private String generateReference() {
        String uuid = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return "CLM-" + uuid;
    }

    // ── Mapper Claim → ClaimResponse
    private ClaimResponse mapToResponse(
            Claim claim, String message) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .reference(claim.getReference())
                .policyNumber(claim.getPolicyNumber())
                .claimType(claim.getClaimType())
                .status(claim.getStatus())
                .clientEmail(claim.getClientEmail())
                .confidenceScore(claim.getConfidenceScore())
                .photoUrls(claim.getPhotoUrls())
                .createdAt(claim.getCreatedAt())
                // ← AJOUTER
                .clientEstimatedCost(claim.getClientEstimatedCost())
                .vehicleBrand(claim.getVehicleBrand())
                .vehicleModel(claim.getVehicleModel())
                .vehicleYear(claim.getVehicleYear())
                .vehicleCategory(claim.getVehicleCategory())
                // ← AJOUTER
                .description(claim.getDescription())
                .incidentLocation(claim.getIncidentLocation())
                .incidentDate(claim.getIncidentDate())
                .clientId(claim.getClientId())
                .message("Sinistre soumis avec succès. Référence : "
                        + claim.getReference())
                .build();
    }
}
