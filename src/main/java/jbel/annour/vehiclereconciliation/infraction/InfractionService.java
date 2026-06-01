package jbel.annour.vehiclereconciliation.infraction;

import jbel.annour.vehiclereconciliation.util.MatriculeUtils;
import jbel.annour.vehiclereconciliation.vehicle.Vehicle;
import jbel.annour.vehiclereconciliation.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InfractionService {

    private final InfractionRepository infractionRepository;
    private final VehicleRepository vehicleRepository;
    private final DataFormatter dataFormatter = new DataFormatter(Locale.FRANCE);

    public List<Infraction> findAll() {
        return infractionRepository.findAll();
    }

    public Infraction findById(Long id) {
        return infractionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Infraction introuvable"));
    }

    public Infraction create(Infraction infraction) {
        infraction.setId(null);
        prepareVehicleLink(infraction);
        return infractionRepository.save(infraction);
    }

    public Infraction update(Long id, Infraction infraction) {
        Infraction existingInfraction = findById(id);
        existingInfraction.setVehicleId(infraction.getVehicleId());
        existingInfraction.setDriverId(infraction.getDriverId());
        existingInfraction.setMatricule(infraction.getMatricule());
        existingInfraction.setNormalizedMatricule(infraction.getNormalizedMatricule());
        existingInfraction.setDriverName(infraction.getDriverName());
        existingInfraction.setInfractionDate(infraction.getInfractionDate());
        existingInfraction.setLocation(infraction.getLocation());
        existingInfraction.setOwner(infraction.getOwner());
        existingInfraction.setTenant(infraction.getTenant());
        existingInfraction.setType(infraction.getType());
        existingInfraction.setAmount(infraction.getAmount());
        existingInfraction.setPoints(infraction.getPoints());
        existingInfraction.setPaymentStatus(infraction.getPaymentStatus());
        existingInfraction.setReference(infraction.getReference());
        existingInfraction.setStatus(infraction.getStatus());
        existingInfraction.setNotes(infraction.getNotes());
        prepareVehicleLink(existingInfraction);
        return infractionRepository.save(existingInfraction);
    }

    public void delete(Long id) {
        infractionRepository.deleteById(id);
    }

    @Transactional
    public void relinkInfractionsForVehicle(Vehicle vehicle) {
        if (vehicle == null || vehicle.getNormalizedMatricule() == null || vehicle.getNormalizedMatricule().isBlank()) {
            return;
        }

        infractionRepository.findAll()
                .stream()
                .filter(infraction -> vehicle.getNormalizedMatricule().equals(MatriculeUtils.normalize(infraction.getMatricule())))
                .forEach(infraction -> {
                    infraction.setNormalizedMatricule(vehicle.getNormalizedMatricule());
                    infraction.setVehicle(vehicle);
                    infraction.setVehicleId(vehicle.getId());
                    infraction.setStatus("OK");
                    infractionRepository.save(infraction);
                });
    }

    public int importNarsaExcel(MultipartFile file) {
        int imported = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
                return imported;
            }

            Map<String, Integer> columns = readColumns(sheet.getRow(0));

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String reference = cellText(row, columns, "numero du pv", "numero pv", "pv", "reference");
                if (reference == null || reference.isBlank() || infractionRepository.existsByReference(reference)) {
                    continue;
                }

                Infraction infraction = new Infraction();
                infraction.setReference(reference);
                infraction.setInfractionDate(cellDate(row, columns, "date d infraction", "date dinfraction", "date infraction"));
                infraction.setLocation(cellText(row, columns, "lieu d infraction", "lieu dinfraction", "lieu infraction", "lieu"));
                infraction.setMatricule(cellText(row, columns, "matricule"));
                infraction.setOwner(cellText(row, columns, "proprietaire", "propriétaire"));
                infraction.setTenant(cellText(row, columns, "locataire"));
                infraction.setPaymentStatus(cellText(row, columns, "situation du pv", "situation pv", "paiement"));
                infraction.setType(cellText(row, columns, "l infraction", "linfraction", "infraction", "type"));
                infraction.setAmount(cellAmount(row, columns, "montant"));
                infraction.setPoints(cellInteger(row, columns, "nombre point", "nombre points", "points"));
                prepareVehicleLink(infraction);

                infractionRepository.save(infraction);
                imported++;
            }

            return imported;
        } catch (IOException e) {
            throw new IllegalArgumentException("Impossible de lire le fichier Excel NARSA", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Fichier Excel NARSA invalide", e);
        }
    }

    private void prepareVehicleLink(Infraction infraction) {
        String normalizedMatricule = MatriculeUtils.normalize(infraction.getMatricule());
        infraction.setNormalizedMatricule(normalizedMatricule);

        if (normalizedMatricule == null) {
            infraction.setVehicle(null);
            infraction.setVehicleId(null);
            infraction.setStatus("VEHICULE_INTROUVABLE");
            return;
        }

        Optional<Vehicle> vehicle = vehicleRepository.findByNormalizedMatricule(normalizedMatricule);
        if (vehicle.isPresent()) {
            infraction.setVehicle(vehicle.get());
            infraction.setVehicleId(vehicle.get().getId());
            infraction.setStatus("OK");
        } else {
            infraction.setVehicle(null);
            infraction.setVehicleId(null);
            infraction.setStatus("VEHICULE_INTROUVABLE");
        }
    }

    private Map<String, Integer> readColumns(Row headerRow) {
        Map<String, Integer> columns = new HashMap<>();
        if (headerRow == null) {
            return columns;
        }

        for (Cell cell : headerRow) {
            String header = normalizeHeader(dataFormatter.formatCellValue(cell));
            if (!header.isBlank()) {
                columns.put(header, cell.getColumnIndex());
            }
        }
        return columns;
    }

    private String cellText(Row row, Map<String, Integer> columns, String... aliases) {
        Integer columnIndex = columnIndex(columns, aliases);
        if (columnIndex == null) {
            return null;
        }

        String value = dataFormatter.formatCellValue(row.getCell(columnIndex));
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDate cellDate(Row row, Map<String, Integer> columns, String... aliases) {
        Integer columnIndex = columnIndex(columns, aliases);
        if (columnIndex == null) {
            return null;
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        String value = dataFormatter.formatCellValue(cell).trim();
        if (value.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next common NARSA export format.
            }
        }
        return null;
    }

    private BigDecimal cellAmount(Row row, Map<String, Integer> columns, String... aliases) {
        String value = cellText(row, columns, aliases);
        if (value == null) {
            return null;
        }

        String normalized = value.replace(" ", "").replace(",", ".").replaceAll("[^0-9.\\-]", "");
        if (normalized.isBlank() || "-".equals(normalized)) {
            return null;
        }
        return new BigDecimal(normalized);
    }

    private Integer cellInteger(Row row, Map<String, Integer> columns, String... aliases) {
        String value = cellText(row, columns, aliases);
        if (value == null) {
            return null;
        }

        String normalized = value.replaceAll("[^0-9\\-]", "");
        if (normalized.isBlank() || "-".equals(normalized)) {
            return null;
        }
        return Integer.valueOf(normalized);
    }

    private Integer columnIndex(Map<String, Integer> columns, String... aliases) {
        for (String alias : aliases) {
            Integer columnIndex = columns.get(normalizeHeader(alias));
            if (columnIndex != null) {
                return columnIndex;
            }
        }
        return null;
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("'", " ")
                .replaceAll("[^A-Za-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
        return normalized;
    }

}
