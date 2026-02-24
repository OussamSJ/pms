package com.pms.pms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable  // Hibernate refuse tout UPDATE ou DELETE sur cette entité
@Table(
    name = "logs_actions"
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "type_action", nullable = false, length = 50)
    private String typeAction;
    /*
     * Valeurs possibles :
     * IMPORT_CSV          - import d'un fichier CSV
     * CHECK_IN            - client checké en
     * PAIEMENT_ACCEPTE    - POS retourne OK
     * PAIEMENT_REFUSE     - POS retourne KO
     * BRACELET_DISTRIBUE  - bracelet activé
     * CLE_SORTIE          - clé remise au client
     * CLE_RENDUE          - clé retournée
     * MENAGE_OK           - ménage validé par staff
     * SEJOUR_CLOTURE      - séjour clôturé
     * USER_CREATED        - création compte utilisateur
     */

    @Column(name = "booking_id", length = 50)
    private String bookingId;

    @Column(length = 50)
    private String utilisateur; // username du staff ou "BORNE" pour actions automatiques

    @Column(columnDefinition = "TEXT")
    private String detail; // Informations complémentaires (ex: référence POS, IP borne)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}


