package com.pms.pms.services;

import com.pms.pms.dto.Dtos;
import com.pms.pms.exceptions.GlobalExceptionHandler.BusinessException;
import com.pms.pms.exceptions.GlobalExceptionHandler.ResourceNotFoundException;
import com.pms.pms.models.*;
import com.pms.pms.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

	
    private final ReservationRepository reservationRepo;
    private final CasierRepository casierRepo;
    private final CleRepository cleRepo;
    private final BraceletRepository braceletRepo;
    private final LogActionRepository logRepo;
    
    
    
    
  
    
    
    
    

    // ─── Lecture ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Dtos.ReservationResponse> getArriveesDuJour() {
        return reservationRepo
            .findByDateArriveeAndStatutNot(LocalDate.now(), StatutReservation.CLOTURE)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<Dtos.ReservationResponse> getRetoursDuJour() {
        return reservationRepo
            .findByDateDepartAndStatutIn(LocalDate.now(),
                List.of(StatutReservation.CLE_SORTIE, StatutReservation.CLE_RENDUE))
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Dtos.ReservationResponse getByBookingId(String bookingId) {
        return toResponse(findOrThrow(bookingId));
    }

    @Transactional(readOnly = true)
    public List<Dtos.ReservationResponse> searchByNom(String nom) {
        return reservationRepo.findByNomClientContainingIgnoreCase(nom)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Dtos.BorneVerifResponse verifierPourBorne(String bookingId) {
        Reservation r = findOrThrow(bookingId);
        boolean paiementOk = isPaiementValide(r);
        boolean cleOk = r.getStatut() == StatutReservation.PAIEMENT_OK
                     || r.getStatut() == StatutReservation.CLE_SORTIE;
        String casierNum = r.getCasier() != null ? r.getCasier().getNumero() : null;
        String cleNum    = r.getCle()    != null ? r.getCle().getNumero()    : null;
        return new Dtos.BorneVerifResponse(
            r.getBookingId(), r.getNomClient(), r.getStatut(),
            paiementOk, cleOk, casierNum, cleNum, r.getBracelets().size()
        );
    }

    @Transactional(readOnly = true)
    public Dtos.DashboardStats getDashboardStats() {
        LocalDate today = LocalDate.now();
        return new Dtos.DashboardStats(
            reservationRepo.findByDateArriveeAndStatutNot(today, StatutReservation.CLOTURE).size(),
            reservationRepo.findByDateDepartAndStatutIn(today, List.of(StatutReservation.CLE_SORTIE, StatutReservation.CLE_RENDUE)).size(),
            reservationRepo.findByStatut(StatutReservation.EN_ATTENTE_PAIEMENT).size(),
            reservationRepo.findByStatut(StatutReservation.PAIEMENT_OK).size(),
            reservationRepo.findByStatut(StatutReservation.CLE_SORTIE).size(),
            reservationRepo.findByStatut(StatutReservation.CLE_RENDUE).size()
        );
    }

    // ─── Check-in ───────────────────────────────────────────────────────────

    /**
     * Flux terrain : Paiement OK → attribution casier → distribution bracelets → clé.
     * Appelé par la borne après confirmation POS.
     */
    @Transactional
    public Dtos.ReservationResponse effectuerCheckIn(Dtos.CheckInRequest req, String operateur) {
        Reservation r = findOrThrow(req.bookingId());

        // 1. Paiement requis
        if (!isPaiementValide(r)) {
            throw new BusinessException("Paiement non validé – check-in impossible pour " + req.bookingId());
        }

        // 2. Casier : attribution si pas encore assigné
        if (r.getCasier() == null) {
            Casier casier = casierRepo.findFirstByDisponible(true)
                .orElseThrow(() -> new BusinessException("Aucun casier disponible"));
            casier.setDisponible(false);
            casier.setReservation(r);
            r.setCasier(casierRepo.save(casier));
        }

        // 3. Bracelets
        if (req.codesRfidBracelets() != null) {
            for (String rfid : req.codesRfidBracelets()) {
                if (braceletRepo.existsByCodeRfid(rfid)) continue; // déjà enregistré
                Bracelet b = Bracelet.builder()
                    .codeRfid(rfid).actif(true).reservation(r).build();
                r.getBracelets().add(braceletRepo.save(b));
            }
            log(LogType.BRACELET_DISTRIBUE, r.getBookingId(), operateur,
                req.codesRfidBracelets().size() + " bracelet(s)");
        }

        // 4. Clé
        if (r.getCle() == null) {
            Cle cle = cleRepo.findFirstByStatut(StatutCle.DISPONIBLE)
                .orElseThrow(() -> new BusinessException("Aucune clé disponible"));
            cle.setStatut(StatutCle.SORTIE);
            cle.setReservation(r);
            r.setCle(cleRepo.save(cle));
            log(LogType.CLE_SORTIE, r.getBookingId(), operateur, "Clé " + cle.getNumero());
        }

        // 5. Statut
        r.setStatut(StatutReservation.CLE_SORTIE);
        log(LogType.CHECK_IN, r.getBookingId(), operateur, null);

        return toResponse(reservationRepo.save(r));
    }

    // ─── Check-out ──────────────────────────────────────────────────────────

    @Transactional
    public Dtos.ReservationResponse rendreCle(String bookingId, String operateur) {
        Reservation r = findOrThrow(bookingId);

        if (r.getStatut() != StatutReservation.CLE_SORTIE) {
            throw new BusinessException("La clé n'est pas en état SORTIE pour " + bookingId);
        }

        Cle cle = r.getCle();
        if (cle != null) {
            cle.setStatut(StatutCle.RENDUE);
            cleRepo.save(cle);
        }

        r.setStatut(StatutReservation.CLE_RENDUE);
        log(LogType.CLE_RENDUE, bookingId, operateur, cle != null ? "Clé " + cle.getNumero() : null);

        return toResponse(reservationRepo.save(r));
    }

    @Transactional
    public Dtos.ReservationResponse validerMenage(String bookingId, String operateur) {
        Reservation r = findOrThrow(bookingId);

        if (r.getStatut() != StatutReservation.CLE_RENDUE) {
            throw new BusinessException("La clé doit être rendue avant de valider le ménage");
        }

        // Libération casier
        if (r.getCasier() != null) {
            Casier casier = r.getCasier();
            casier.setDisponible(true);
            casier.setReservation(null);
            casierRepo.save(casier);
        }

        // Remise à dispo de la clé
        if (r.getCle() != null) {
            Cle cle = r.getCle();
            cle.setStatut(StatutCle.DISPONIBLE);
            cle.setReservation(null);
            cleRepo.save(cle);
        }

        r.setStatut(StatutReservation.MENAGE_OK);
        log(LogType.MENAGE_OK, bookingId, operateur, null);

        return toResponse(reservationRepo.save(r));
    }

    @Transactional
    public Dtos.ReservationResponse cloturerSejour(String bookingId, String operateur) {
        Reservation r = findOrThrow(bookingId);

        if (r.getStatut() != StatutReservation.MENAGE_OK) {
            throw new BusinessException("Le ménage doit être validé avant la clôture");
        }

        r.setStatut(StatutReservation.CLOTURE);
        log(LogType.SEJOUR_CLOTURE, bookingId, operateur, null);

        return toResponse(reservationRepo.save(r));
    }

    // ─── Utilitaires ────────────────────────────────────────────────────────

    public Reservation findOrThrow(String bookingId) {
        return reservationRepo.findByBookingId(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable : " + bookingId));
    }

    public boolean isPaiementValide(Reservation r) {
        return r.getPaiements().stream()
            .anyMatch(p -> p.getStatut() == StatutPaiement.ACCEPTE);
    }

    private void log(LogType type, String bookingId, String utilisateur, String detail) {
        logRepo.save(LogAction.builder()
            .typeAction(type.name())
            .bookingId(bookingId)
            .utilisateur(utilisateur)
            .detail(detail)
            .build());
    }

    private enum LogType {
        CHECK_IN, BRACELET_DISTRIBUE, CLE_SORTIE, CLE_RENDUE, MENAGE_OK, SEJOUR_CLOTURE
    }

    public Dtos.ReservationResponse toResponse(Reservation r) {
        return new Dtos.ReservationResponse(
            r.getId(), r.getBookingId(), r.getNomClient(), r.getPrenomClient(),
            r.getDateArrivee(), r.getDateDepart(), r.getMobilHome(),
            r.getNombreAdultes(), r.getNombreEnfants(),
            r.getSoldeAPayer(), r.getTaxeSejourEstimee(), r.getStatut(),
            r.getCasier() != null ? r.getCasier().getNumero() : null,
            r.getCle()    != null ? r.getCle().getNumero()    : null,
            r.getBracelets().stream().map(Bracelet::getCodeRfid).toList(),
            isPaiementValide(r),
            r.getCreatedAt()
        );
    }
}
