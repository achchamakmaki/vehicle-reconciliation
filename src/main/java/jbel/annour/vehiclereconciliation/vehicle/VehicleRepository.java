package jbel.annour.vehiclereconciliation.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    long countByStatusIgnoreCase(String status);

    Optional<Vehicle> findByNormalizedMatricule(String normalizedMatricule);
}
