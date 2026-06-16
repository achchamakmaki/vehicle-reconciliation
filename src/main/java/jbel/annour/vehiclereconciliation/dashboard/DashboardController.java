package jbel.annour.vehiclereconciliation.dashboard;

import jbel.annour.vehiclereconciliation.fuel.FuelConsumption;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<FleetDashboardStats> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/recent-fuel-consumptions")
    public ResponseEntity<List<FuelConsumption>> getRecentFuelConsumptions() {
        return ResponseEntity.ok(dashboardService.getRecentFuelConsumptions());
    }
}
