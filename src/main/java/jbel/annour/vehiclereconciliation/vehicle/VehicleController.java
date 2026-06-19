package jbel.annour.vehiclereconciliation.vehicle;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;
    private final SageX3VehicleService sageX3VehicleService;
    private final SageVehicleSyncService sageVehicleSyncService;

    @GetMapping
    public ResponseEntity<List<Vehicle>> findAll() {
        List<Vehicle> vehicles = vehicleService.findAll();
        log.debug("Vehicles REST response - count={}", vehicles.size());
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/sage-x3")
    public ResponseEntity<List<Vehicle>> findSageX3Vehicles() {
        return ResponseEntity.ok(sageX3VehicleService.fetchVehicleList());
    }

    @GetMapping("/sage-x3/raw")
    public ResponseEntity<JsonNode> findSageX3VehiclesRaw() {
        return ResponseEntity.ok(sageX3VehicleService.fetchVehicles());
    }

    @PostMapping("/sync-sage")
    public ResponseEntity<?> syncSageVehicles() {
        try {
            return ResponseEntity.ok(sageVehicleSyncService.syncVehicles());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                            "message",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Impossible de synchroniser les vehicules depuis Sage X3."
                    ));
        }
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<Vehicle> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(@RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(vehicleService.create(vehicle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> update(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(vehicleService.update(id, vehicle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
