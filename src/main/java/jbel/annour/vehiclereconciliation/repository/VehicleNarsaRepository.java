package jbel.annour.vehiclereconciliation.repository;

import jbel.annour.vehiclereconciliation.entity.VehicleNarsa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleNarsaRepository extends JpaRepository<VehicleNarsa, Long> {

    List<VehicleNarsa> findByNormalizedMatricule(String normalizedMatricule);
}