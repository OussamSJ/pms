package com.pms.pms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "paiements",
    indexes = {
        @Index(name = "idx_paiement_reservation", columnList = "reservation_id"),
        @Index(name = "idx_paiement_statut",      columnList = "statut"),
        @Index(name = "idx_paiement_ref_pos",     columnList = "reference_pos")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal montant;

    @PositiveOrZero
    @Column(name = "taxe_sejour", precision = 10, scale = 2)
    private BigDecimal taxeSejour;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPaiement statut;

    // Identifiant de transaction retourné par le POS externe
    // Unique pour garantir l'idempotence (pas de doublon si webhook relancé)
    @Column(name = "reference_pos", unique = true, length = 100)
    private String referencePos;

    @CreationTimestamp
    @Column(name = "date_paiement", nullable = false, updatable = false)
    private LocalDateTime datePaiement;
}