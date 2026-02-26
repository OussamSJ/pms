package com.pms.pms.repositories;

import com.pms.pms.models.LogAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogActionRepository extends JpaRepository<LogAction, Long> {

    List<LogAction> findByBookingIdOrderByCreatedAtDesc(String bookingId);

    List<LogAction> findByTypeActionOrderByCreatedAtDesc(String typeAction);

    List<LogAction> findTop50ByOrderByCreatedAtDesc();
}
