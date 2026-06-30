package com.insureflow.claimintakeservice.model;

public enum ClaimStatus {
    SUBMITTED,        // Soumis par le client
    ROUTING,          // Agent Routeur en cours
    VALIDATING,       // Agent Validateur en cours
    ESTIMATING,       // Agent Estimateur en cours
    AGGREGATING,      // Calcul score global
    HUMAN_REQUIRED,   // Score < 0.75 → admin requis
    COMPLETED,        // Traitement automatique terminé
    FAILED,           // Erreur durant le traitement
    APPROVED,         // Approuvé par l'admin
    REJECTED          // Sinistre rejeté
}