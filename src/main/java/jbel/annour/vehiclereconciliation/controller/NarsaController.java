package jbel.annour.vehiclereconciliation.controller;

import jbel.annour.vehiclereconciliation.entity.ComparisonResult;
import jbel.annour.vehiclereconciliation.repository.ComparisonResultRepository;
import jbel.annour.vehiclereconciliation.repository.VehicleNarsaRepository;
import jbel.annour.vehiclereconciliation.repository.VehicleSageRepository;
import jbel.annour.vehiclereconciliation.service.ComparisonService;
import jbel.annour.vehiclereconciliation.service.NarsaImportService;
import jbel.annour.vehiclereconciliation.service.SageImportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
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

    @GetMapping("/results/export")
    public ResponseEntity<byte[]> exportResults() throws IOException {
        List<ComparisonResult> results = comparisonResultRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Comparison Results");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] columns = {"Matricule", "Status", "Details"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
                header.getCell(i).setCellStyle(headerStyle);
            }

            for (int i = 0; i < results.size(); i++) {
                ComparisonResult result = results.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(valueOrEmpty(result.getMatricule()));
                row.createCell(1).setCellValue(valueOrEmpty(result.getStatus()));
                row.createCell(2).setCellValue(valueOrEmpty(result.getDetails()));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comparison-results.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(outputStream.toByteArray());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(new DashboardStats(
                vehicleNarsaRepository.count(),
                vehicleSageRepository.count(),
                comparisonResultRepository.countByStatus("MATCH"),
                comparisonResultRepository.countByStatus("ABSENT_IN_SAGE"),
                comparisonResultRepository.countByStatus("ABSENT_IN_NARSA")
        ));
    }

    @DeleteMapping("/reset")
    public ResponseEntity<String> resetData() {
        comparisonResultRepository.deleteAll();
        vehicleNarsaRepository.deleteAll();
        vehicleSageRepository.deleteAll();

        return ResponseEntity.ok("Données supprimées avec succès");
    }

    public record DashboardStats(
            long totalNarsaVehicles,
            long totalSageVehicles,
            long matchCount,
            long absentInSageCount,
            long absentInNarsaCount
    ) {
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
