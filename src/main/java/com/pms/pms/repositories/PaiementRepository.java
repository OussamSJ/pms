package com.pms.pms.repositories;

import com.pms.pms.models.Paiement;
import com.pms.pms.models.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findByReservationId(Long reservationId);

    Optional<Paiement> findByReferencePos(String referencePos);

    boolean existsByReferencePos(String referencePos);

    boolean existsByReservationIdAndStatut(Long reservationId, StatutPaiement statut);
}
