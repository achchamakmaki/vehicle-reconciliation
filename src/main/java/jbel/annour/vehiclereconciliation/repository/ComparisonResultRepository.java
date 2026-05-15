package jbel.annour.vehiclereconciliation.repository;

import jbel.annour.vehiclereconciliation.entity.ComparisonResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComparisonResultRepository extends JpaRepository<ComparisonResult, Long> {
}
