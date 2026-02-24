package com.pms.pms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(
    name = "bracelets",
    indexes = {
        @Index(name = "idx_bracelet_rfid", columnList = "code_rfid")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bracelet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "code_rfid", nullable = false, unique = true, length = 50)
    private String codeRfid;

    @Builder.Default
    @Column(nullable = false)
    private Boolean actif = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}