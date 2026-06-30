package com.insureflow.claimintakeservice.client;


import com.insureflow.claimintakeservice.dto.OrchestratorRequest;
import com.insureflow.claimintakeservice.dto.OrchestratorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// ── name = nom exact du service dans Eureka
// ── fallback = classe appelée si le service est indisponible
@FeignClient(
        name = "orchestrator-service",
        fallback = OrchestratorClientFallback.class
)
public interface OrchestratorClient {

    // Envoyer un sinistre à l'orchestrateur pour traitement
    @PostMapping("/api/orchestrator/process")
    OrchestratorResponse processClaim(
            @RequestBody OrchestratorRequest request);

    // Récupérer l'état du workflow d'un sinistre
    @GetMapping("/api/orchestrator/workflow/{claimId}")
    OrchestratorResponse getWorkflowStatus(
            @PathVariable Long claimId);
}
