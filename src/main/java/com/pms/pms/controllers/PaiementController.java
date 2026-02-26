package com.pms.pms.controllers;

import com.pms.pms.dto.Dtos;
import com.pms.pms.services.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API paiements POS.
 * Base : /api/paiements
 */
@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    /**
     * POST /api/paiements/pos-webhook
     *
     * Point d'entrée du système POS externe.
     * Enregistre ACCEPTE ou REFUSE et met à jour le statut de la réservation.
     * Idempotent : rejouer le webhook avec la même referencePos est sans effet.
     *
     * Body : { bookingId, statut, montant, taxeSejour, referencePos }
     */
    @PostMapping("/pos-webhook")
    public ResponseEntity<Dtos.PaiementResponse> posWebhook(
            @Valid @RequestBody Dtos.PaiementPosRequest req) {
        return ResponseEntity.ok(paiementService.enregistrerStatutPos(req));
    }

    /**
     * GET /api/paiements/{bookingId}
     * Historique des paiements d'une réservation.
     */
    @GetMapping("/{bookingId}")
    public List<Dtos.PaiementResponse> getPaiements(@PathVariable String bookingId) {
        return paiementService.getPaiementsParReservation(bookingId);
    }
}
