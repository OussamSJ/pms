package com.pms.pms.controllers;

import com.pms.pms.dto.Dtos;
import com.pms.pms.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * API dédiée à la borne autonome de check-in / check-out.
 * Base : /api/borne
 *
 * Ces endpoints sont appelés directement par la borne (authentification par clé API à ajouter).
 */
@RestController
@RequestMapping("/api/borne")
@RequiredArgsConstructor
public class BorneController {

    private final ReservationService reservationService;

    /**
     * GET /api/borne/verifier/{bookingId}
     * Vérification légère : la borne sait si le paiement est validé et si la clé est autorisée.
     */
    @GetMapping("/verifier/{bookingId}")
    public Dtos.BorneVerifResponse verifier(@PathVariable String bookingId) {
        return reservationService.verifierPourBorne(bookingId);
    }

    /**
     * POST /api/borne/checkin
     * Déclenche l'attribution casier + bracelets + clé si paiement valide.
     */
    @PostMapping("/checkin")
    public Dtos.ReservationResponse checkIn(
            @Valid @RequestBody Dtos.CheckInRequest req) {
        return reservationService.effectuerCheckIn(req, "BORNE");
    }

    /**
     * POST /api/borne/rendre-cle
     * Enregistre le retour de clé lors du check-out.
     */
    @PostMapping("/rendre-cle")
    public Dtos.ReservationResponse rendreCle(
            @Valid @RequestBody Dtos.RendreCleRequest req) {
        return reservationService.rendreCle(req.bookingId(), "BORNE");
    }
}
