package com.pms.pms.repositories;

import com.pms.pms.models.Bracelet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BraceletRepository extends JpaRepository<Bracelet, Long> {

    Optional<Bracelet> findByCodeRfid(String codeRfid);

    List<Bracelet> findByReservationId(Long reservationId);

    boolean existsByCodeRfid(String codeRfid);
}
