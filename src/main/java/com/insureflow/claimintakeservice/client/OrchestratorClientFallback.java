package com.insureflow.claimintakeservice.client;

import com.insureflow.claimintakeservice.dto.OrchestratorRequest;
import com.insureflow.claimintakeservice.dto.OrchestratorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrchestratorClientFallback
        implements OrchestratorClient {

    @Override
    public OrchestratorResponse processClaim(
            OrchestratorRequest request) {
        log.warn("orchestrator-service indisponible. " +
                        "Fallback activé pour sinistre : {}",
                request.getClaimId());

        return OrchestratorResponse.builder()
                .claimId(request.getClaimId())
                .success(false)
                .message("Service temporairement indisponible. " +
                        "Le sinistre sera traité ultérieurement.")
                .build();
    }

    @Override
    public OrchestratorResponse getWorkflowStatus(
            Long claimId) {
        log.warn("orchestrator-service indisponible. " +
                        "Impossible de récupérer le workflow : {}",
                claimId);

        return OrchestratorResponse.builder()
                .claimId(claimId)
                .success(false)
                .message("Statut temporairement indisponible.")
                .build();
    }
}