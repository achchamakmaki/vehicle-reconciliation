package jbel.annour.vehiclereconciliation.dashboard;

import jbel.annour.vehiclereconciliation.fuel.FuelConsumptionRepository;
import jbel.annour.vehiclereconciliation.fuel.FuelConsumption;
import jbel.annour.vehiclereconciliation.infraction.InfractionRepository;
import jbel.annour.vehiclereconciliation.repository.ComparisonResultRepository;
import jbel.annour.vehiclereconciliation.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VehicleRepository vehicleRepository;
    private final InfractionRepository infractionRepository;
    private final FuelConsumptionRepository fuelConsumptionRepository;
    private final ComparisonResultRepository comparisonResultRepository;

    public FleetDashboardStats getStats() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate startOfNextMonth = startOfMonth.plusMonths(1);

        var totalFuelAmount = fuelConsumptionRepository.sumTotalAmount();
        var monthlyFuelAmount = fuelConsumptionRepository.sumAmountBetween(startOfMonth, startOfNextMonth);
        long fuelAnomaliesCount = fuelConsumptionRepository.countAnomalies();

        return new FleetDashboardStats(
                vehicleRepository.count(),
                vehicleRepository.countByStatusIgnoreCase("CONFORME"),
                infractionRepository.countByPaymentStatusIgnoreCase("NON_PAYE"),
                totalFuelAmount,
                monthlyFuelAmount,
                fuelAnomaliesCount,
                totalFuelAmount,
                monthlyFuelAmount,
                fuelAnomaliesCount,
                comparisonResultRepository.countByStatus("ABSENT_IN_SAGE"),
                comparisonResultRepository.countByStatus("ABSENT_IN_NARSA")
        );
    }

    public List<FuelConsumption> getRecentFuelConsumptions() {
        return fuelConsumptionRepository.findTop5ByOrderByIdDesc();
    }
}
