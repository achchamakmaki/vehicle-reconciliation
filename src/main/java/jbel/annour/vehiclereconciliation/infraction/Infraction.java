package jbel.annour.vehiclereconciliation.infraction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jbel.annour.vehiclereconciliation.vehicle.Vehicle;
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
    private String normalizedMatricule;
    private String driverName;
    private LocalDate infractionDate;
    private String location;
    private String owner;
    private String tenant;
    private String type;
    private BigDecimal amount;
    private Integer points;
    private String paymentStatus;
    private String reference;
    private String status;
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_vehicle_id")
    @JsonIgnore
    private Vehicle vehicle;
}
