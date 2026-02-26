package com.pms.pms.repositories;

import com.pms.pms.models.Casier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CasierRepository extends JpaRepository<Casier, Long> {

    Optional<Casier> findByNumero(String numero);

    Optional<Casier> findByReservationId(Long reservationId);

    List<Casier> findByDisponible(Boolean disponible);

    Optional<Casier> findFirstByDisponible(Boolean disponible);
}
