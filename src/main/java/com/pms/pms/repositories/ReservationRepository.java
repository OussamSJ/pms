package com.pms.pms.repositories;

import com.pms.pms.models.Reservation;
import com.pms.pms.models.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByBookingId(String bookingId);

    boolean existsByBookingId(String bookingId);

    /** Arrivées du jour (toutes sauf CLOTURE) */
    List<Reservation> findByDateArriveeAndStatutNot(LocalDate date, StatutReservation statut);

    /** Retours du jour (CLÉ_SORTIE ou CLÉ_RENDUE) */
    List<Reservation> findByDateDepartAndStatutIn(LocalDate date, List<StatutReservation> statuts);

    /** Recherche par nom (insensible à la casse) */
    List<Reservation> findByNomClientContainingIgnoreCase(String nomClient);

    /** Réservations par statut */
    List<Reservation> findByStatut(StatutReservation statut);

    /** Arrivées à venir (pour dashboard) */
    @Query("SELECT r FROM Reservation r WHERE r.dateArrivee BETWEEN :from AND :to AND r.statut != 'CLOTURE' ORDER BY r.dateArrivee")
    List<Reservation> findArriveesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
