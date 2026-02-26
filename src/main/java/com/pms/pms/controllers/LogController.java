package com.pms.pms.controllers;

import com.pms.pms.models.LogAction;
import com.pms.pms.repositories.LogActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API lecture des logs d'actions.
 * Base : /api/logs
 *
 * Lecture seule – aucune suppression possible (entité @Immutable).
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogActionRepository logRepo;

    /** GET /api/logs — 50 dernières actions */
    @GetMapping
    public List<LogAction> getLogs() {
        return logRepo.findTop50ByOrderByCreatedAtDesc();
    }

    /** GET /api/logs/{bookingId} — historique d'une réservation */
    @GetMapping("/{bookingId}")
    public List<LogAction> getLogsByReservation(@PathVariable String bookingId) {
        return logRepo.findByBookingIdOrderByCreatedAtDesc(bookingId);
    }
}
