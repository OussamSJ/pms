package com.pms.pms.repositories;

import com.pms.pms.models.Cle;
import com.pms.pms.models.StatutCle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CleRepository extends JpaRepository<Cle, Long> {

    Optional<Cle> findByNumero(String numero);

    Optional<Cle> findByReservationId(Long reservationId);

    List<Cle> findByStatut(StatutCle statut);

    /** Première clé disponible pour auto-attribution */
    Optional<Cle> findFirstByStatut(StatutCle statut);
}
