package jbel.annour.vehiclereconciliation.dashboard;

import java.math.BigDecimal;

public record FleetDashboardStats(
        long totalVehicles,
        long compliantVehicles,
        long unpaidInfractions,
        BigDecimal totalFuelAmount,
        BigDecimal monthlyFuelAmount,
        long fuelAnomaliesCount,
        BigDecimal totalFuelConsumption,
        BigDecimal currentMonthFuelConsumption,
        long detectedAnomalies,
        long vehiclesAbsentInSage,
        long vehiclesAbsentInNarsa
) {
}
