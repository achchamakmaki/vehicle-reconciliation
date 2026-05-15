package jbel.annour.vehiclereconciliation.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ComparisonResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matricule;
    private String status;
    private String details;
}