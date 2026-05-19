package jbel.annour.vehiclereconciliation.reconciliation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, Long> {

    long countByStatus(String status);
}
