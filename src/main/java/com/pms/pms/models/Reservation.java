package com.pms.pms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "reservations",
    indexes = {
        @Index(name = "idx_resa_booking_id",   columnList = "booking_id"),
        @Index(name = "idx_resa_date_arrivee", columnList = "date_arrivee"),
        @Index(name = "idx_resa_date_depart",  columnList = "date_depart"),
        @Index(name = "idx_resa_statut",       columnList = "statut"),
        @Index(name = "idx_resa_nom_client",   columnList = "nom_client")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "booking_id", nullable = false, unique = true, length = 50)
    private String bookingId;

    @NotBlank
    @Column(name = "nom_client", nullable = false, length = 100)
    private String nomClient;

    @Column(name = "prenom_client", length = 100)
    private String prenomClient;

    @NotNull
    @Column(name = "date_arrivee", nullable = false)
    private LocalDate dateArrivee;

    @NotNull
    @Column(name = "date_depart", nullable = false)
    private LocalDate dateDepart;

    @Column(name = "mobil_home", length = 20)
    private String mobilHome;

    @PositiveOrZero
    @Builder.Default
    @Column(name = "nombre_adultes", nullable = false)
    private Integer nombreAdultes = 1;

    @PositiveOrZero
    @Builder.Default
    @Column(name = "nombre_enfants", nullable = false)
    private Integer nombreEnfants = 0;

    @PositiveOrZero
    @Builder.Default
    @Column(name = "solde_a_payer", nullable = false, precision = 10, scale = 2)
    private BigDecimal soldeAPayer = BigDecimal.ZERO;

    @PositiveOrZero
    @Builder.Default
    @Column(name = "taxe_sejour_estimee", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxeSejourEstimee = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private StatutReservation statut = StatutReservation.A_ARRIVER;

    // ---- Relations ----

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL,
              fetch = FetchType.LAZY, orphanRemoval = true)
    private Casier casier;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL,
              fetch = FetchType.LAZY, orphanRemoval = true)
    private Cle cle;

    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Bracelet> bracelets = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Paiement> paiements = new ArrayList<>();

    // ---- Audit ----

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ---- Méthodes utilitaires ----

    /** Vérifie si la réservation a au moins un paiement accepté 
    public boolean isPaiementValide() {
        return paiements.stream()
                .anyMatch(p -> p.getStatut() == StatutPaiement.ACCEPTE);
    }

    Vérifie si la clé peut être remise (paiement OK + bracelets distribués) 
    public boolean isCleAutorisee() {
        return statut == StatutReservation.PAIEMENT_OK
               || statut == StatutReservation.CLE_SORTIE;
    }*/
}


