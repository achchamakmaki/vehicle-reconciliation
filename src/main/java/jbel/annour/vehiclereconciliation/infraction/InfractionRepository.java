package jbel.annour.vehiclereconciliation.infraction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InfractionRepository extends JpaRepository<Infraction, Long> {

    long countByPaymentStatusIgnoreCase(String paymentStatus);

    boolean existsByReference(String reference);

    List<Infraction> findByNormalizedMatricule(String normalizedMatricule);
}
