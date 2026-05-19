package jbel.annour.vehiclereconciliation.reconciliation;

import jbel.annour.vehiclereconciliation.controller.NarsaController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final NarsaController narsaController;

    @GetMapping("/stats")
    public ResponseEntity<NarsaController.DashboardStats> getStats() {
        return narsaController.getStats();
    }
}
