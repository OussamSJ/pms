package com.pms.pms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(
    name = "casiers"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Casier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Builder.Default
    @Column(nullable = false)
    private Boolean disponible = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}