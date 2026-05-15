package jbel.annour.vehiclereconciliation.controller;

import jbel.annour.vehiclereconciliation.entity.ComparisonResult;
import jbel.annour.vehiclereconciliation.repository.ComparisonResultRepository;
import jbel.annour.vehiclereconciliation.repository.VehicleNarsaRepository;
import jbel.annour.vehiclereconciliation.repository.VehicleSageRepository;
import jbel.annour.vehiclereconciliation.service.ComparisonService;
import jbel.annour.vehiclereconciliation.service.NarsaImportService;
import jbel.annour.vehiclereconciliation.service.SageImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/narsa")
@RequiredArgsConstructor
public class NarsaController {

    private final NarsaImportService narsaImportService;
    private final ComparisonService comparisonService;
    private final SageImportService sageImportService;
    private final ComparisonResultRepository comparisonResultRepository;
    private final VehicleNarsaRepository vehicleNarsaRepository;
    private final VehicleSageRepository vehicleSageRepository;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile file
    ) {
        narsaImportService.importExcel(file);
        return ResponseEntity.ok("Excel imported successfully");
    }

    @PostMapping("/compare")
    public ResponseEntity<String> compare() {
        comparisonService.compareVehicles();
        return ResponseEntity.ok("Comparison completed");
    }
    @PostMapping("/sage/upload")
    public ResponseEntity<String> uploadSage(
            @RequestParam("file") MultipartFile file) {

        sageImportService.importExcel(file);
        return ResponseEntity.ok("Sage Excel imported successfully");
    }

    @GetMapping("/results")
    public ResponseEntity<List<ComparisonResult>> getResults() {
        return ResponseEntity.ok(comparisonResultRepository.findAll());
    }

    @DeleteMapping("/reset")
    public ResponseEntity<String> resetData() {
        comparisonResultRepository.deleteAll();
        vehicleNarsaRepository.deleteAll();
        vehicleSageRepository.deleteAll();

        return ResponseEntity.ok("Données supprimées avec succès");
    }
}