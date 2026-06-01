package jbel.annour.vehiclereconciliation.service;

import jbel.annour.vehiclereconciliation.entity.VehicleSage;
import jbel.annour.vehiclereconciliation.repository.VehicleSageRepository;
import jbel.annour.vehiclereconciliation.util.MatriculeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SageImportService {

    private final VehicleSageRepository vehicleSageRepository;

    public void importExcel(MultipartFile file) {
        vehicleSageRepository.deleteAll();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> columns = readHeaderColumns(sheet.getRow(0), formatter);

            int sageCodeColumn = resolveColumn(columns, 0, "numero dossier", "n dossier", "no dossier", "num dossier");
            int numeroColumn = resolveColumn(
                    columns,
                    1,
                    "numero enregistrement",
                    "numero denregistrement",
                    "numero d enregistrement",
                    "n enregistrement",
                    "n d enregistrement",
                    "no enregistrement",
                    "num enregistrement"
            );
            int lettreColumn = resolveColumn(columns, 2, "lettre");
            int prefectureColumn = resolveColumn(columns, 3, "prefecture", "ville", "code prefecture");
            int marqueColumn = optionalColumn(columns, "marque", "brand", "constructeur");
            int modeleColumn = optionalColumn(columns, "modele", "model", "version");
            int statutColumn = optionalColumn(columns, "statut", "status", "etat");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String sageCode = cellValue(row, sageCodeColumn, formatter);
                String numero = cellValue(row, numeroColumn, formatter);
                String lettre = cellValue(row, lettreColumn, formatter);
                String prefecture = cellValue(row, prefectureColumn, formatter);
                String matricule = buildMatricule(numero, lettre, prefecture);

                if (matricule.isBlank()) {
                    continue;
                }

                VehicleSage vehicle = new VehicleSage();
                vehicle.setSageCode(sageCode);
                vehicle.setNoImmatriculation(matricule);
                vehicle.setNormalizedMatricule(MatriculeUtils.normalize(matricule));
                vehicle.setMarque(cellValue(row, marqueColumn, formatter));
                vehicle.setModele(cellValue(row, modeleColumn, formatter));
                vehicle.setStatut(cellValue(row, statutColumn, formatter));

                vehicleSageRepository.save(vehicle);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur import Excel Sage : " + e.getMessage(), e);
        }
    }

    private Map<String, Integer> readHeaderColumns(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> columns = new HashMap<>();
        if (headerRow == null) {
            return columns;
        }

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String header = normalizeHeader(formatter.formatCellValue(headerRow.getCell(i)));
            if (!header.isBlank()) {
                columns.put(header, i);
            }
        }

        return columns;
    }

    private int resolveColumn(Map<String, Integer> columns, int fallbackIndex, String... names) {
        for (String name : names) {
            Integer index = columns.get(normalizeHeader(name));
            if (index != null) {
                return index;
            }
        }

        for (Map.Entry<String, Integer> column : columns.entrySet()) {
            for (String name : names) {
                if (containsAllTokens(column.getKey(), normalizeHeader(name))) {
                    return column.getValue();
                }
            }
        }

        return fallbackIndex;
    }

    private int optionalColumn(Map<String, Integer> columns, String... names) {
        for (String name : names) {
            Integer index = columns.get(normalizeHeader(name));
            if (index != null) {
                return index;
            }
        }

        for (Map.Entry<String, Integer> column : columns.entrySet()) {
            for (String name : names) {
                if (containsAllTokens(column.getKey(), normalizeHeader(name))) {
                    return column.getValue();
                }
            }
        }

        return -1;
    }

    private String cellValue(Row row, int column, DataFormatter formatter) {
        if (column < 0) {
            return "";
        }

        return formatter.formatCellValue(row.getCell(column)).trim();
    }

    private String buildMatricule(String numero, String lettre, String prefecture) {
        if (numero.isBlank() || lettre.isBlank() || prefecture.isBlank()) {
            return "";
        }

        return numero.trim() + "-" + lettre.trim() + "-" + prefecture.trim();
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents
                .trim()
                .toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAllTokens(String header, String expected) {
        String[] tokens = expected.split(" ");
        for (String token : tokens) {
            if (!token.isBlank() && !header.contains(token)) {
                return false;
            }
        }

        return true;
    }
}
