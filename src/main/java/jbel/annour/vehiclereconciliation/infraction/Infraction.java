package jbel.annour.vehiclereconciliation.infraction;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fleet_infractions")
@Data
public class Infraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vehicleId;
    private Long driverId;
    private String matricule;
    private String driverName;
    private LocalDate infractionDate;
    private String location;
    private String type;
    private BigDecimal amount;
    private String paymentStatus;
    private String reference;
    private String notes;
}
