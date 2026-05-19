package jbel.annour.vehiclereconciliation.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    long countByStatusIgnoreCase(String status);
}
