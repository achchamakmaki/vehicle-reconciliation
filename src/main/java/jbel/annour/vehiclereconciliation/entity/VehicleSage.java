package jbel.annour.vehiclereconciliation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class VehicleSage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String noImmatriculation;
    private String normalizedMatricule;
    private String sageCode;

    private String marque;
    private String modele;
    private String statut;
}
