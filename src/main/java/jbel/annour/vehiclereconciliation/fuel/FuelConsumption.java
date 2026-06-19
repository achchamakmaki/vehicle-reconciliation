package jbel.annour.vehiclereconciliation.fuel;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fleet_fuel_consumptions")
@Data
public class FuelConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vehicleId;
    private Long driverId;
    private String matricule;
    private String driverName;
    private String station;
    private LocalDate consumptionDate;
    private LocalDateTime receivedAt;
    private BigDecimal liters;
    private BigDecimal amount;
    private String receiptPhotoPath;
    private String receiptPhotoUrl;
    private String status;
    private String source;
    private String notes;
    private String invoiceNumber;
    private String product;
    private String paymentMethod;
    private String fuelTime;
    private BigDecimal unitPrice;
}
