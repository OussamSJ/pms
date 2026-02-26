package com.pms.pms.services;

import com.pms.pms.dto.Dtos;
import com.pms.pms.exceptions.GlobalExceptionHandler.DuplicateResourceException;
import com.pms.pms.models.*;
import com.pms.pms.repositories.LogActionRepository;
import com.pms.pms.repositories.PaiementRepository;
import com.pms.pms.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaiementService {
	@Autowired
    private final PaiementRepository paiementRepo;
    private final ReservationRepository reservationRepo;
    private final LogActionRepository logRepo;
    private final ReservationService reservationService;

    /**
     * Point d'entrée du webhook POS.
     * Idempotent : une même referencePos ne crée jamais deux enregistrements.
     */
    @Transactional
    public Dtos.PaiementResponse enregistrerStatutPos(Dtos.PaiementPosRequest req) {

        // Idempotence – évite les doublons si le webhook est rejoué
        if (paiementRepo.existsByReferencePos(req.referencePos())) {
            log.info("Paiement déjà enregistré pour referencePos={}", req.referencePos());
            return toPaiementResponse(
                paiementRepo.findByReferencePos(req.referencePos()).orElseThrow()
            );
        }

        Reservation reservation = reservationService.findOrThrow(req.bookingId());

        Paiement paiement = Paiement.builder()
            .reservation(reservation)
            .montant(req.montant())
            .taxeSejour(req.taxeSejour())
            .statut(req.statut())
            .referencePos(req.referencePos())
            .build();

        Paiement saved = paiementRepo.save(paiement);

        // Mise à jour statut réservation
        if (req.statut() == StatutPaiement.ACCEPTE) {
            reservation.setStatut(StatutReservation.PAIEMENT_OK);
            reservationRepo.save(reservation);
            log("PAIEMENT_ACCEPTE", req.bookingId(), "BORNE",
                "Ref POS: " + req.referencePos() + " | Montant: " + req.montant());
        } else {
            reservation.setStatut(StatutReservation.EN_ATTENTE_PAIEMENT);
            reservationRepo.save(reservation);
            log("PAIEMENT_REFUSE", req.bookingId(), "BORNE",
                "Ref POS: " + req.referencePos());
        }

        return toPaiementResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<Dtos.PaiementResponse> getPaiementsParReservation(String bookingId) {
        Reservation r = reservationService.findOrThrow(bookingId);
        return paiementRepo.findByReservationId(r.getId())
            .stream().map(this::toPaiementResponse).toList();
    }

    private void log(String type, String bookingId, String utilisateur, String detail) {
        logRepo.save(LogAction.builder()
            .typeAction(type)
            .bookingId(bookingId)
            .utilisateur(utilisateur)
            .detail(detail)
            .build());
    }

    private Dtos.PaiementResponse toPaiementResponse(Paiement p) {
        return new Dtos.PaiementResponse(
            p.getId(),
            p.getReservation().getBookingId(),
            p.getMontant(),
            p.getTaxeSejour(),
            p.getStatut(),
            p.getReferencePos(),
            p.getDatePaiement()
        );
    }
}
