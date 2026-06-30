package com.insureflow.claimintakeservice.config;

import org.springframework.context.annotation.Configuration;


// ── Cette classe est prête pour ajouter Resilience4j
// quand tu implémenteras les agents IA
// Pour l'instant elle documente la stratégie
// de tolérance aux pannes

// Stratégie de fallback pour claim-intake :
//
// 1. Feign appelle orchestrator-service
//    ↓ Si indisponible
// 2. OrchestratorClientFallback retourne réponse vide
//    ↓
// 3. Le sinistre est sauvegardé avec statut SUBMITTED
//    ↓
// 4. Un job schedulé réessaiera plus tard



@Configuration
public class CircuitBreakerConfig {
}
