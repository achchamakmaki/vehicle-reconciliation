package jbel.annour.vehiclereconciliation.fuel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FuelConsumptionRepository extends JpaRepository<FuelConsumption, Long> {

    @Query("select coalesce(sum(f.amount), 0) from FuelConsumption f")
    BigDecimal sumTotalAmount();

    @Query("select coalesce(sum(f.amount), 0) from FuelConsumption f where f.consumptionDate >= :startDate and f.consumptionDate < :endDate")
    BigDecimal sumAmountBetween(LocalDate startDate, LocalDate endDate);

    @Query("select count(f) from FuelConsumption f where f.liters is null or f.amount is null or f.liters <= 0 or f.amount <= 0")
    long countAnomalies();

    @Query("select f from FuelConsumption f order by case when f.receivedAt is null then 1 else 0 end, f.receivedAt desc, f.id desc")
    List<FuelConsumption> findAllOrderByReceivedAtDesc();

    @Query(value = "select f from FuelConsumption f order by case when f.receivedAt is null then 1 else 0 end, f.receivedAt desc, f.id desc")
    List<FuelConsumption> findTop5OrderByReceivedAtDesc(org.springframework.data.domain.Pageable pageable);
}
