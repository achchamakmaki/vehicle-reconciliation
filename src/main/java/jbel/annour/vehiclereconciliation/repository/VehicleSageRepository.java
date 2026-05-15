package jbel.annour.vehiclereconciliation.repository;

import jbel.annour.vehiclereconciliation.entity.VehicleSage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleSageRepository extends JpaRepository<VehicleSage, Long> {

    List<VehicleSage> findByNormalizedMatricule(String normalizedMatricule);
}
