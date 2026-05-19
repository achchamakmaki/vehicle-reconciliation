package jbel.annour.vehiclereconciliation.infraction;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InfractionRepository extends JpaRepository<Infraction, Long> {

    long countByPaymentStatusIgnoreCase(String paymentStatus);
}
