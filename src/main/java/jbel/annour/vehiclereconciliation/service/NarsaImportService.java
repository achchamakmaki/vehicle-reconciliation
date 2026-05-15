package jbel.annour.vehiclereconciliation.service;

import jbel.annour.vehiclereconciliation.entity.VehicleNarsa;
import jbel.annour.vehiclereconciliation.repository.VehicleNarsaRepository;
import jbel.annour.vehiclereconciliation.util.MatriculeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NarsaImportService {

    private final VehicleNarsaRepository vehicleNarsaRepository;

    public void importExcel(MultipartFile file) {
        vehicleNarsaRepository.deleteAll();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String matricule = formatter.formatCellValue(row.getCell(0)).trim();
                String statut = formatter.formatCellValue(row.getCell(3)).trim();
                String motifRejet = formatter.formatCellValue(row.getCell(4)).trim();
                String ww = formatter.formatCellValue(row.getCell(5)).trim();

                if (matricule.isBlank()) {
                    continue;
                }

                VehicleNarsa vehicle = new VehicleNarsa();
                vehicle.setNoImmatriculation(matricule);
                vehicle.setNormalizedMatricule(MatriculeUtils.normalize(matricule));
                vehicle.setStatut(statut);
                vehicle.setMotifRejet(motifRejet);
                vehicle.setWw(ww);

                vehicleNarsaRepository.save(vehicle);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur import Excel NARSA : " + e.getMessage());
        }
    }
}