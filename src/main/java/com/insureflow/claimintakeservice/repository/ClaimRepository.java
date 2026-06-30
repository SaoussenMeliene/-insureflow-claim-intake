package com.insureflow.claimintakeservice.repository;

import com.insureflow.claimintakeservice.model.Claim;
import com.insureflow.claimintakeservice.model.ClaimStatus;
import com.insureflow.claimintakeservice.model.ClaimType;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository
        extends JpaRepository<Claim, Long> {

    // ════════════════════════════════════════════
    // RECHERCHES SIMPLES
    // ════════════════════════════════════════════

    // Trouver par référence CLM-XXXX
    Optional<Claim> findByReference(String reference);

    // Trouver par numéro de police
    List<Claim> findByPolicyNumber(String policyNumber);

    // Trouver par client (trié par date décroissante)
    List<Claim> findByClientIdOrderByCreatedAtDesc(
            String clientId);

    // Trouver par statut
    List<Claim> findByStatus(ClaimStatus status);

    // Trouver par type de sinistre
    List<Claim> findByClaimType(ClaimType claimType);

    // Trouver par email client
    List<Claim> findByClientEmail(String clientEmail);

    // ════════════════════════════════════════════
    // RECHERCHES COMBINÉES
    // ════════════════════════════════════════════

    // Sinistres d'un client avec un statut précis
    List<Claim> findByClientIdAndStatus(
            String clientId,
            ClaimStatus status);

    // Sinistres d'un client avec un type précis
    List<Claim> findByClientIdAndClaimType(
            String clientId,
            ClaimType claimType);

    // ════════════════════════════════════════════
    // RECHERCHES AVEC SCORE DE CONFIANCE
    // ════════════════════════════════════════════

    // Sinistres avec score inférieur à un seuil
    // → Utilisé pour identifier les cas HUMAN_REQUIRED
    List<Claim> findByConfidenceScoreLessThan(
            Double threshold);

    // Sinistres avec score supérieur ou égal à un seuil
    List<Claim> findByConfidenceScoreGreaterThanEqual(
            Double threshold);

    // ════════════════════════════════════════════
    // RECHERCHES PAR DATE
    // ════════════════════════════════════════════

    // Sinistres entre deux dates d'incident
    List<Claim> findByIncidentDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    // ════════════════════════════════════════════
    // REQUÊTES JPQL PERSONNALISÉES
    // ════════════════════════════════════════════

    // Compter les sinistres par statut
    @Query("SELECT COUNT(c) FROM Claim c " +
            "WHERE c.status = :status")
    Long countByStatus(
            @Param("status") ClaimStatus status);

    // Compter les sinistres d'un client
    @Query("SELECT COUNT(c) FROM Claim c " +
            "WHERE c.clientId = :clientId")
    Long countByClientId(
            @Param("clientId") String clientId);

    // Sinistres nécessitant une révision humaine
    // (score < 0.75 OU statut HUMAN_REQUIRED)
    @Query("SELECT c FROM Claim c WHERE " +
            "c.status = 'HUMAN_REQUIRED' OR " +
            "(c.confidenceScore IS NOT NULL AND " +
            "c.confidenceScore < :threshold)")
    List<Claim> findClaimsRequiringHumanReview(
            @Param("threshold") Double threshold);

    // Sinistres en cours de traitement
    // (tous les statuts sauf COMPLETED et REJECTED)
    @Query("SELECT c FROM Claim c WHERE " +
            "c.status NOT IN " +
            "('COMPLETED', 'REJECTED', 'SUBMITTED')")
    List<Claim> findClaimsInProgress();

    // Vérifier si une référence existe déjà
    boolean existsByReference(String reference);

    // Vérifier si un numéro de police existe
    boolean existsByPolicyNumber(String policyNumber);
}
