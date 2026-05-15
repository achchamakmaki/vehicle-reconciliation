package jbel.annour.vehiclereconciliation.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class VehicleNarsa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String noImmatriculation;
    private String normalizedMatricule;
    private String statut;
    private String motifRejet;
    private String ww;
}