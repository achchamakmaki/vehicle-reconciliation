package jbel.annour.vehiclereconciliation.service;

import jbel.annour.vehiclereconciliation.entity.VehicleSage;
import jbel.annour.vehiclereconciliation.repository.VehicleSageRepository;
import jbel.annour.vehiclereconciliation.util.MatriculeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SageImportService {

    private final VehicleSageRepository vehicleSageRepository;

    public void importExcel(MultipartFile file) {
        vehicleSageRepository.deleteAll();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String matricule = formatter.formatCellValue(row.getCell(0));

                String[] parts = matricule.split("-");

                if(parts.length == 3){
                    matricule = parts[2] + "-" + parts[1] + "-" + parts[0];
                }
                String marque = formatter.formatCellValue(row.getCell(1));
                String modele = formatter.formatCellValue(row.getCell(2));
                String statut = formatter.formatCellValue(row.getCell(3));

                if (matricule.isBlank()) continue;

                VehicleSage vehicle = new VehicleSage();
                vehicle.setNoImmatriculation(matricule);
                vehicle.setNormalizedMatricule(
                        MatriculeUtils.normalize(matricule)
                );
                vehicle.setMarque(marque);
                vehicle.setModele(modele);
                vehicle.setStatut(statut);

                vehicleSageRepository.save(vehicle);
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}