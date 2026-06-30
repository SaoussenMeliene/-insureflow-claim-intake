package com.insureflow.claimintakeservice.service;

import com.insureflow.claimintakeservice.dto.ClaimRequest;
import com.insureflow.claimintakeservice.dto.ClaimResponse;
import com.insureflow.claimintakeservice.model.Claim;
import com.insureflow.claimintakeservice.model.ClaimStatus;
import com.insureflow.claimintakeservice.model.ClaimType;
import com.insureflow.claimintakeservice.repository.ClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private ClaimService claimService;

    private Claim claim;
    private ClaimRequest request;

    @BeforeEach
    void setUp() {
        claim = new Claim();
        claim.setId(1L);
        claim.setReference("CLM-2024-001");
        claim.setClaimType(ClaimType.AUTO);
        claim.setDescription("Accident voiture Tunis");
        claim.setClientId("15");
        claim.setClientEmail("ali@gmail.com");
        claim.setClientEstimatedCost(800.0);
        claim.setStatus(ClaimStatus.SUBMITTED);

        request = new ClaimRequest();
        request.setClaimType(ClaimType.AUTO);
        request.setDescription("Accident voiture Tunis");
        request.setClientId("15");
        request.setClientEmail("ali@gmail.com");
        request.setClientEstimatedCost(800.0);
        request.setPolicyNumber("202650000000037");
    }

    // ════════════════════════════════════════════
    // Test 1 — Déclarer un sinistre
    // ════════════════════════════════════════════
    @Test
    void testSubmitClaim_Succes() {
        when(claimRepository.save(any(Claim.class)))
                .thenReturn(claim);

        ClaimResponse response = claimService
                .submitClaim(request);

        assertNotNull(response);
        assertEquals("CLM-2024-001", response.getReference());
        // ✅ Corrigé — compare String avec String
        assertEquals(
                ClaimStatus.SUBMITTED.name(),
                response.getStatus().toString()
        );
        verify(claimRepository, times(1)).save(any());
    }

    // ════════════════════════════════════════════
    // Test 2 — Récupérer sinistres par client
    // ════════════════════════════════════════════
    @Test
    void testGetClaimsByClient_Succes() {
        List<Claim> claims = List.of(claim, claim, claim);

        when(claimRepository
                .findByClientIdOrderByCreatedAtDesc("15"))
                .thenReturn(claims);

        List<ClaimResponse> result = claimService
                .getClaimsByClient("15");

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    // ════════════════════════════════════════════
    // Test 3 — Client sans sinistres
    // ════════════════════════════════════════════
    @Test
    void testGetClaimsByClient_Empty() {
        when(claimRepository
                .findByClientIdOrderByCreatedAtDesc("99"))
                .thenReturn(List.of());

        List<ClaimResponse> result = claimService
                .getClaimsByClient("99");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ════════════════════════════════════════════
    // Test 4 — Récupérer par ID
    // ════════════════════════════════════════════
    @Test
    void testGetClaimById_Succes() {
        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        ClaimResponse response = claimService
                .getClaimById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    // ════════════════════════════════════════════
    // Test 5 — Sinistre inexistant
    // ════════════════════════════════════════════
    @Test
    void testGetClaimById_NotFound() {
        when(claimRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                claimService.getClaimById(99L)
        );
    }

    // ════════════════════════════════════════════
    // Test 6 — Mettre à jour le statut
    // ════════════════════════════════════════════
    @Test
    void testUpdateClaimStatus_Succes() {
        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class)))
                .thenReturn(claim);

        ClaimResponse response = claimService
                .updateClaimStatus(1L, ClaimStatus.APPROVED);

        assertNotNull(response);
        verify(claimRepository, times(1)).save(any());
    }

    // ════════════════════════════════════════════
    // Test 7 — Score de confiance
    // ════════════════════════════════════════════
    @Test
    void testUpdateConfidenceScore_Succes() {
        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class)))
                .thenReturn(claim);

        ClaimResponse response = claimService
                .updateConfidenceScore(1L, 0.89);

        assertNotNull(response);
        verify(claimRepository, times(1)).save(any());
    }

    // ════════════════════════════════════════════
// Test 8 — Supprimer sinistre
// ════════════════════════════════════════════
    @Test
    void testDeleteClaim_Succes() {
        // ✅ existsById — pas findById !
        when(claimRepository.existsById(1L))
                .thenReturn(true);
        doNothing().when(claimRepository)
                .deleteById(1L);

        claimService.deleteClaim(1L);

        verify(claimRepository, times(1)).deleteById(1L);
    }

    // ════════════════════════════════════════════
// Test bonus — Supprimer sinistre inexistant
// ════════════════════════════════════════════
    @Test
    void testDeleteClaim_NotFound() {
        when(claimRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                claimService.deleteClaim(99L)
        );
    }
    // ════════════════════════════════════════════
    // Test 9 — Récupérer par référence
    // ════════════════════════════════════════════
    @Test
    void testGetClaimByReference_Succes() {
        when(claimRepository
                .findByReference("CLM-2024-001"))
                .thenReturn(Optional.of(claim));

        ClaimResponse response = claimService
                .getClaimByReference("CLM-2024-001");

        assertNotNull(response);
        assertEquals("CLM-2024-001", response.getReference());
    }

    // ════════════════════════════════════════════
    // Test 10 — Récupérer tous les sinistres
    // ════════════════════════════════════════════
    @Test
    void testGetAllClaims_Succes() {
        List<Claim> claims = List.of(claim, claim);
        when(claimRepository.findAll())
                .thenReturn(claims);

        List<ClaimResponse> result = claimService
                .getAllClaims();

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}