package com.pms.pms.controllers;

import com.pms.pms.dto.Dtos;
import com.pms.pms.services.ImportCsvService;
import com.pms.pms.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * API Back-office réservations.
 * Base : /api/reservations
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ImportCsvService importCsvService;

    // ─── Lecture ────────────────────────────────────────────────────────────

    /** GET /api/reservations/arrivees-du-jour */
    @GetMapping("/arrivees-du-jour")
    public List<Dtos.ReservationResponse> getArriveesDuJour() {
        return reservationService.getArriveesDuJour();
    }

    /** GET /api/reservations/retours-du-jour */
    @GetMapping("/retours-du-jour")
    public List<Dtos.ReservationResponse> getRetoursDuJour() {
        return reservationService.getRetoursDuJour();
    }

    /** GET /api/reservations/{bookingId} */
    @GetMapping("/{bookingId}")
    public Dtos.ReservationResponse getByBookingId(@PathVariable String bookingId) {
        return reservationService.getByBookingId(bookingId);
    }

    /** GET /api/reservations/search?nom=Dupont */
    @GetMapping("/search")
    public List<Dtos.ReservationResponse> search(@RequestParam String nom) {
        return reservationService.searchByNom(nom);
    }

    /** GET /api/reservations/dashboard */
    @GetMapping("/dashboard")
    public Dtos.DashboardStats getDashboard() {
        return reservationService.getDashboardStats();
    }

    // ─── Import CSV ──────────────────────────────────────────────────────────

    /** POST /api/reservations/import — multipart/form-data */
    @PostMapping("/import")
    public ResponseEntity<Dtos.ImportResult> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "operateur", defaultValue = "ADMIN") String operateur) {
        Dtos.ImportResult result = importCsvService.importerCsv(file, operateur);
        return ResponseEntity.ok(result);
    }

    // ─── Actions back-office ─────────────────────────────────────────────────

    /** POST /api/reservations/{bookingId}/menage-ok */
    @PostMapping("/{bookingId}/menage-ok")
    public Dtos.ReservationResponse validerMenage(
            @PathVariable String bookingId,
            @RequestParam(defaultValue = "STAFF") String operateur) {
        return reservationService.validerMenage(bookingId, operateur);
    }

    /** POST /api/reservations/{bookingId}/cloturer */
    @PostMapping("/{bookingId}/cloturer")
    public Dtos.ReservationResponse cloturer(
            @PathVariable String bookingId,
            @RequestParam(defaultValue = "STAFF") String operateur) {
        return reservationService.cloturerSejour(bookingId, operateur);
    }
}
