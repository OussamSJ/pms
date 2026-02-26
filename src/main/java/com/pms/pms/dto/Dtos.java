package com.pms.pms.dto;

import com.pms.pms.models.StatutPaiement;
import com.pms.pms.models.StatutReservation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// ─── Réservation ─────────────────────────────────────────────────────────────

public class Dtos {

    /** Réponse réservation (lecture) */
    public record ReservationResponse(
        Long id,
        String bookingId,
        String nomClient,
        String prenomClient,
        LocalDate dateArrivee,
        LocalDate dateDepart,
        String mobilHome,
        Integer nombreAdultes,
        Integer nombreEnfants,
        BigDecimal soldeAPayer,
        BigDecimal taxeSejourEstimee,
        StatutReservation statut,
        String casierNumero,
        String cleNumero,
        List<String> braceletsRfid,
        boolean paiementValide,
        LocalDateTime createdAt
    ) {}

    /** Webhook POS → enregistrement statut paiement */
    public record PaiementPosRequest(
        @NotBlank String bookingId,
        @NotNull StatutPaiement statut,
        @PositiveOrZero BigDecimal montant,
        @PositiveOrZero BigDecimal taxeSejour,
        @NotBlank String referencePos      // identifiant unique transaction POS
    ) {}

    /** Réponse paiement */
    public record PaiementResponse(
        Long id,
        String bookingId,
        BigDecimal montant,
        BigDecimal taxeSejour,
        StatutPaiement statut,
        String referencePos,
        LocalDateTime datePaiement
    ) {}

    // ─── Check-in / Check-out ────────────────────────────────────────────────

    /** Demande check-in depuis borne */
    public record CheckInRequest(
        @NotBlank String bookingId,
        List<String> codesRfidBracelets   // RFID des bracelets à activer
    ) {}

    /** Retour clé */
    public record RendreCleRequest(
        @NotBlank String bookingId
    ) {}

    /** Validation ménage */
    public record ValidationMenageRequest(
        @NotBlank String bookingId,
        @NotBlank String utilisateur
    ) {}

    // ─── Import CSV ──────────────────────────────────────────────────────────

    /** Résultat d'un import CSV */
    public record ImportResult(
        int importes,
        int ignores,        // doublons
        int erreurs,
        List<String> details
    ) {}

    // ─── Vérification borne ─────────────────────────────────────────────────

    /** Réponse légère pour la borne */
    public record BorneVerifResponse(
        String bookingId,
        String nomClient,
        StatutReservation statut,
        boolean paiementValide,
        boolean cleAutorisee,
        String casierNumero,
        String cleNumero,
        int nombreBracelets
    ) {}

    // ─── Dashboard ──────────────────────────────────────────────────────────

    public record DashboardStats(
        long arriveesDuJour,
        long departsDuJour,
        long enAttentePaiement,
        long paiementOk,
        long cleSortie,
        long menageAFaire
    ) {}
}
