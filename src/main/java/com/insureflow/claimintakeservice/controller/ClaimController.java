package com.insureflow.claimintakeservice.controller;

import com.insureflow.claimintakeservice.dto.ClaimRequest;
import com.insureflow.claimintakeservice.dto.ClaimResponse;
import com.insureflow.claimintakeservice.model.ClaimStatus;
import com.insureflow.claimintakeservice.service.ClaimService;
import com.insureflow.claimintakeservice.service.FileStorageService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Slf4j
public class ClaimController {

    private final ClaimService claimService;
    private final FileStorageService fileStorageService;

    // ✅ Variables Groq depuis application.yml
    @Value("${groq.api-key}")
    private String groqApiKey;



    // ════════════════════════════════════════════
    // POST /api/claims (multipart)
    // Soumettre un sinistre AVEC photos
    // ════════════════════════════════════════════
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClaimResponse> submitClaimWithPhotos(
            @RequestPart("claim") ClaimRequest request,
            @RequestPart(value = "photos", required = false)
            List<MultipartFile> photos) {

        log.info("POST /api/claims (multipart) — client : {}",
                request.getClientId());

        if (photos != null && !photos.isEmpty()) {
            List<String> photoUrls =
                    fileStorageService.storeFiles(photos);
            request.setPhotoUrls(photoUrls);
            log.info("{} photo(s) stockée(s)", photoUrls.size());
        }

        ClaimResponse response = claimService.submitClaim(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ════════════════════════════════════════════
    // GET /api/claims
    // ════════════════════════════════════════════
    @GetMapping
    public ResponseEntity<List<ClaimResponse>> getAllClaims() {
        log.info("GET /api/claims — tous les sinistres");
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    // ════════════════════════════════════════════
    // GET /api/claims/{id}
    // ════════════════════════════════════════════
    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(
            @PathVariable Long id) {
        log.info("GET /api/claims/{}", id);
        return ResponseEntity.ok(claimService.getClaimById(id));
    }

    // ════════════════════════════════════════════
    // GET /api/claims/reference/{reference}
    // ════════════════════════════════════════════
    @GetMapping("/reference/{reference}")
    public ResponseEntity<ClaimResponse> getClaimByReference(
            @PathVariable String reference) {
        log.info("GET /api/claims/reference/{}", reference);
        return ResponseEntity.ok(
                claimService.getClaimByReference(reference));
    }

    // ════════════════════════════════════════════
    // GET /api/claims/client/{clientId}
    // ════════════════════════════════════════════
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ClaimResponse>> getByClient(
            @PathVariable String clientId) {
        log.info("GET /api/claims/client/{}", clientId);
        return ResponseEntity.ok(
                claimService.getClaimsByClient(clientId));
    }

    // ════════════════════════════════════════════
    // GET /api/claims/status/{status}
    // ════════════════════════════════════════════
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ClaimResponse>> getByStatus(
            @PathVariable ClaimStatus status) {
        log.info("GET /api/claims/status/{}", status);
        return ResponseEntity.ok(
                claimService.getClaimsByStatus(status));
    }

    // ════════════════════════════════════════════
    // PATCH /api/claims/{id}/status
    // ════════════════════════════════════════════
    @PatchMapping("/{id}/status")
    public ResponseEntity<ClaimResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ClaimStatus status) {
        log.info("PATCH /api/claims/{}/status → {}", id, status);
        return ResponseEntity.ok(
                claimService.updateClaimStatus(id, status));
    }

    // ════════════════════════════════════════════
    // PATCH /api/claims/{id}/score
    // ════════════════════════════════════════════
    @PatchMapping("/{id}/score")
    public ResponseEntity<ClaimResponse> updateScore(
            @PathVariable Long id,
            @RequestParam Double score) {
        log.info("PATCH /api/claims/{}/score → {}", id, score);
        return ResponseEntity.ok(
                claimService.updateConfidenceScore(id, score));
    }

    // ════════════════════════════════════════════
    // DELETE /api/claims/{id}
    // ════════════════════════════════════════════
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClaim(
            @PathVariable Long id) {
        log.info("DELETE /api/claims/{}", id);
        claimService.deleteClaim(id);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════
    // ✅ POST /api/claims/improve-description
    // Assistant IA — améliore la description
    // ════════════════════════════════════════════
    // ════════════════════════════════════════════
    @PostMapping("/ai/improve")
    public ResponseEntity<Map<String, String>> improveDescription(
            @RequestBody Map<String, String> request) {

        String raw = request.get("description");
        log.info("✨ Amélioration description IA : {}",
                raw != null ?
                        raw.substring(0, Math.min(50, raw.length()))
                        : "vide");

        if (raw == null || raw.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Description vide"));
        }

        try {
            // ✅ URL en dur — évite le problème de parsing
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .baseUrl("https://api.groq.com/openai/v1")
                    .apiKey(groqApiKey)
                    .modelName("llama-3.3-70b-versatile")
                    .maxTokens(400)
                    .temperature(0.3)
                    .build();

            String prompt = """
            Tu es un assistant pour les déclarations
            de sinistres d'assurance en Tunisie.
            
            Le client a écrit ceci pour décrire
            son sinistre :
            "%s"
            
            Améliore uniquement la description
            narrative de ce sinistre en français
            professionnel et structuré.
            
            La description doit expliquer :
            - Comment l'incident s'est produit
            - Les circonstances et le contexte
            - Les éléments ou pièces endommagés
            - La gravité apparente des dommages
            
            NE PAS mentionner :
            - Le prix ou coût
            - La date
            - Le lieu
            - Le type ou modèle du véhicule
            
            Réponds UNIQUEMENT avec la description
            améliorée, sans titre ni explication,
            maximum 5 lignes.
            """.formatted(raw);

            String improved = model.generate(prompt);
            log.info("✅ Description améliorée avec succès");

            return ResponseEntity.ok(
                    Map.of("description", improved));

        } catch (Exception e) {
            log.error("❌ Erreur amélioration IA : {}",
                    e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error",
                            "Erreur lors de la génération IA"));
        }
    }
    // ════════════════════════════════════════════
    // GET /api/claims/health
    // ════════════════════════════════════════════
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(
                "claim-intake-service opérationnel ✅");
    }
}